var stompClient = null;
var gameTimerInterval = null;
var totalSeconds = 0; // Общ брой изминали секунди
let gameInProgress;
document.addEventListener('DOMContentLoaded', function() {

    enableButton('startGameButton');
    disableButton('stopGameButton');

    connect();

    document.getElementById('startGameForm').addEventListener('submit', function(event) {
        event.preventDefault();
        startGame();
    });

    document.getElementById('stopGameForm').addEventListener('submit', function(event) {
        event.preventDefault();
        stopGame();
    });

    document.getElementById('addMinerForm').addEventListener('submit', function(event) {
        event.preventDefault();
        addMiner();
    });

    document.getElementById('removeMinerForm').addEventListener('submit', function(event) {
        event.preventDefault();
        var removeMinerId = document.getElementById('removeMinerId').value;
        removeMiner(removeMinerId);
    });
});

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        // Subscribe to the workers topic
        stompClient.subscribe('/topic/workers', function(message) {
            var workersData = JSON.parse(message.body);
            updatePageAndWorkers(workersData);
        });
    }, function(error) {
        console.error('WebSocket connection error:', error);
    });
}

function updatePageAndWorkers(data) {
    var workerPanelsContainer = document.getElementById('workerPanelsContainer');
    var leftResourcesElement = document.getElementById('totalResourcesLeft');

    if (!workerPanelsContainer) {
        console.error('Worker panels container not found.');
        return;
    }

    // Итерирайте през данните за всеки работник и създайте/актуализирайте панелите
    data.forEach(worker => {
        var { id, stopped, totalMinedResources, totalResourcesLeft, totalReceivedMoney, actionMessage } = worker;
        var status = stopped ? 'Inactive' : 'Active';



        // Проверка дали панелът за този работник вече съществува
        var existingPanel = document.getElementById(`workerPanel-${id}`);
        if (!existingPanel) {
            // Създаване на нов панел за работника
            var workerPanel = document.createElement('div');
            workerPanel.id = `workerPanel-${id}`;
            workerPanel.className = 'worker-panel';

            // Създаване на снимка за работника
            var minerInfoDiv = document.createElement('div');
            minerInfoDiv.className = 'miner-info';

            workerPanel.appendChild(minerInfoDiv);

            // Определете цвят на основата на actionMessage
            var actionColorClass = '';
            if (actionMessage === 'Mining...') {
                actionColorClass = 'mining-action';
            } else if (actionMessage === 'Resting...') {
                actionColorClass = 'resting-action';
            } else if (actionMessage === 'Left the mine...'){
                actionColorClass = 'leftTheMine';
            }
            // Създаване на информация за работника
            var workerInfoDiv = document.createElement('div');
            workerInfoDiv.className = 'worker-info';
            workerInfoDiv.innerHTML = `
                <p>Miner: ${id}</p>
                <p>Status: ${status}</p>
                <p>Total Mined Resources: ${totalMinedResources}</p>
                <p>Total Received Money ($): ${totalReceivedMoney}</p>
                <p>Action: <span class="${actionColorClass}">${actionMessage}</span></p>
            `;

            workerPanel.appendChild(workerInfoDiv);
            workerPanelsContainer.appendChild(workerPanel);
        } else {
            // Определете цвят на основата на actionMessage
            if (actionMessage === 'Mining...') {
                actionColorClass = 'mining-action'; // Зелен цвят за Mining...
            } else if (actionMessage === 'Resting...') {
                actionColorClass = 'resting-action'; // Светло син цвят за Resting...
            } else if (actionMessage === 'Left the mine...'){
                actionColorClass = 'leftTheMine';
            }
            // Актуализация на съществуващия панел с новата информация
            existingPanel.querySelector('.worker-info').innerHTML = `
                <p>Miner ID: ${id}</p>
                <p>Status: ${status}</p>
                <p>Total Mined Resources: ${totalMinedResources}</p>
                <p>Total Received Money ($): ${totalReceivedMoney}</p>
                <p>Action: <span class="${actionColorClass}">${actionMessage}</span></p>
            `;
        }

        // Актуализиране на оставащите ресурси
        if (leftResourcesElement) {
            leftResourcesElement.textContent = `Total Resources Left ==> ${totalResourcesLeft}`;
        }
        // Проверка за достигане на 0 оставащи ресурси и спиране на играта
        if (totalResourcesLeft <= 0) {
            totalResourcesLeft=0;
            gameInProgress = false;
            stopGame()
        }
    });
}

function addActionMessage(message) {
    var actionMonitor = document.getElementById('actionMonitor');
    if (!actionMonitor) {
        console.error('Action monitor element not found.');
        return;
    }
    var messageElement = document.createElement('p');
    messageElement.textContent = message;
    actionMonitor.appendChild(messageElement);

    actionMonitor.scrollTop = actionMonitor.scrollHeight;
}

function startGame() {
    var totalResources = parseInt(document.getElementById('totalResources').value);
    var leftResourcesElement = document.getElementById('totalResourcesLeft');
    var initialMiners = parseInt(document.getElementById('initialMiners').value);

    if (leftResourcesElement) {

        leftResourcesElement.textContent = `Total Resources Left ==> ${totalResources}`;
    }
    if (isNaN(totalResources) || totalResources <= 0 ) {
        alert('Please enter valid positive numbers for Total Resources ');
        return;
    }


    fetch('mining-game/start', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            initialMineResources: totalResources,
            initialWorkers: initialMiners
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to start game.');
            }
            return response.json();
        })
        .then(data => {
            gameInProgress = true;
            startTimer();

            addActionMessage('Game started successfully.');

            disableInput('totalResources');
            disableInput('initialMiners');
            disableButton('startGameButton');
            enableButton('stopGameButton');
            enableButton('addMinerButton')

            //updatePageAndWorkers(data.async);
        })
        .catch(error => {
            console.error('Error starting game:', error);
            addActionMessage('Failed to start game.');
        });
}


function stopGame() {
    var initialMiners = parseInt(document.getElementById('initialMiners').value);

    fetch('mining-game/stop', {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to stop game.');
            }
            return response.text();
        })
        .then(data => {
            console.log(data);
            clearInterval(gameTimerInterval);

            addActionMessage("Mining is stopped... ")
            console.log('Timer stopped after game stopped.');

            enableButton('startGameButton');
            disableButton('stopGameButton');
            disableButton('addMinerButton')

            // Промяна на стойността на initialMiners до 0
            document.getElementById('initialMiners').value = 0;
            //updatePageAndWorkers(data);
        })
        .catch(error => {
            console.error('Error stopping game:', error);
        });
}

document.addEventListener('DOMContentLoaded', function() {
    // Добавете слушател за натискане на бутона за рестартиране на играта
    document.getElementById('restartGameButton').addEventListener('click', function() {
        restartGame();
    });
});

function restartGame() {
    // Изпращане на заявка за рестартиране на играта
    fetch('mining-game/restart', {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to restart game...');
            }
            // Редирект към началната страница след успешно рестартиране на играта
            window.location.href = '/'; // Променете URL според вашите нужди
            addActionMessage("The Game is restarted...")
        })
        .catch(error => {
            console.error('Error restarting game:', error);
            addActionMessage('Failed to restart game...');
        });
}


function startTimer() {
    gameTimerInterval = setInterval(function() {
        totalSeconds++;
        updateTimerDisplay();
    }, 1000);
}


function updateTimerDisplay() {
    var formattedTime = new Date(totalSeconds * 1000).toISOString().substr(11, 8);
    document.getElementById('gameTimer').innerText = formattedTime;
}



function addMiner() {
    fetch('mining-game/workers/add', {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to add miner.');
            }
            return response.text();
        })
        .then(data => {
            console.log(data);
            addActionMessage("You added a new Miner...")
        })
        .catch(error => {
            console.error('Error adding miner:', error);
        });
}

function removeMiner() {
    var workerId = document.getElementById('removeMinerId').value;

    // Проверка дали workerId е валидно число
    if (!workerId || isNaN(parseInt(workerId))) {
        console.error('Invalid worker ID:', workerId);
        return;
    }

    // Изпращане на заявка за премахване на работник с даден ID
    fetch(`mining-game/workers/remove/${workerId}`, {
        method: 'POST'
    })
        .then(response => {
            // Проверка дали отговорът е успешен
            if (!response.ok) {
                addActionMessage('Failed to remove miner.');
                throw new Error('Failed to remove miner.');
            }
            return response.text();
        })
        .then(data => {
            console.log(data);
            addActionMessage('You removed Miner '+workerId+' from working process...')
        })
        .catch(error => {
            addActionMessage('Error removing miner ' + workerId);
            console.error('Error removing miner:', error);
        });
}

function enableButton(buttonId) {
    var button = document.getElementById(buttonId);
    if (button) {
        button.classList.remove('inactive-button');
        button.classList.add('active-button');
    }
}

function disableButton(buttonId) {
    var button = document.getElementById(buttonId);
    if (button) {
        button.classList.remove('active-button');
        button.classList.add('inactive-button');
    }
}

function enableInput(inputId) {
    var input = document.getElementById(inputId);
    if (input) {
        input.classList.remove('inactive-button');
        input.classList.add('active-button');
        input.removeAttribute('disabled');
    }
}

function disableInput(inputId) {
    var input = document.getElementById(inputId);
    if (input) {
        input.classList.remove('active-button');
        input.classList.add('inactive-button');
        input.setAttribute('disabled', true);
    }
}
