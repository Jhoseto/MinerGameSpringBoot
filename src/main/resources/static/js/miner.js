var stompClient = null;
var gameTimerInterval = null;
var totalSeconds = 0; // Общ брой изминали секунди

document.addEventListener('DOMContentLoaded', function() {
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

            // Създаване на информация за работника
            var workerInfoDiv = document.createElement('div');
            workerInfoDiv.className = 'worker-info';
            workerInfoDiv.innerHTML = `
                <p>Miner: ${id}</p>
                <p>Status: ${status}</p>
                <p>Total Mined Resources: ${totalMinedResources}</p>
                <p>Total Received Money ($): ${totalReceivedMoney}</p>
                <p>Action: ${actionMessage}</p>    
            `;

            workerPanel.appendChild(workerInfoDiv);
            workerPanelsContainer.appendChild(workerPanel);
        } else {
            // Актуализация на съществуващия панел с новата информация
            existingPanel.querySelector('.worker-info').innerHTML = `
                <p>Miner ID: ${id}</p>
                <p>Status: ${status}</p>
                <p>Total Mined Resources: ${totalMinedResources}</p>
                <p>Total Received Money ($): ${totalReceivedMoney}</p>
                <p>Action Message: ${actionMessage}</p> 
            `;
        }
        // Актуализиране на оставащите ресурси
        if (leftResourcesElement) {
            leftResourcesElement.textContent = `Total Resources Left: ${totalResourcesLeft}`;
        }
        addActionMessage(actionMessage);
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
    // Добавете новият <br> елемент след добавянето на съобщението
    var newlineElement = document.createElement('br');
    actionMonitor.appendChild(newlineElement);
    // Променете скролирането на actionMonitor, за да бъде на последния ред
    actionMonitor.scrollTop = actionMonitor.scrollHeight;
}

function startGame() {
    var totalResources = parseInt(document.getElementById('totalResources').value);
    var initialMiners = parseInt(document.getElementById('initialMiners').value);

    if (isNaN(totalResources) || isNaN(initialMiners) || totalResources <= 0 || initialMiners <= 0) {
        alert('Please enter valid positive numbers for Total Resources and Initial Miners.');
        return;
    }

    fetch('/mining-game/start', {
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
            gameStarted = true; // Установете флага за успешно стартирана игра
            startTimer();
            updatePageAndWorkers(data.workers);
            addActionMessage('Game started successfully.');
        })
        .catch(error => {
            console.error('Error starting game:', error);
            addActionMessage('Failed to start game.');
        });
}


function stopGame() {
    fetch('/mining-game/stop', {
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
            console.log('Timer stopped after game stopped.');
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
    fetch('/mining-game/restart', {
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
    fetch('/mining-game/workers/add', {
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
    fetch(`/mining-game/workers/remove/${workerId}`, {
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
        })
        .catch(error => {
            addActionMessage('Error removing miner ' + workerId);
            console.error('Error removing miner:', error);
        });
}
