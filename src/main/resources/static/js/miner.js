var stompClient = null;
var gameTimerInterval = null;

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
        stompClient.subscribe('/topic/workers', function(workers) {
            updatePageAndWorkers(JSON.parse(workers.body));
        });
        addActionMessage('Connected to server');
    });
}

function updatePageAndWorkers(data) {
    data.forEach(worker => {
        var { id, stopped, totalMinedResources, totalReceivedMoney } = worker;
        var status = stopped ? 'Inactive' : 'Active';

        // Проверка дали панелът за този работник вече съществува
        var existingPanel = document.getElementById(`workerPanel-${id}`);
        if (!existingPanel) {
            // Създаване на нов панел за работника
            var workerPanel = document.createElement('div');
            workerPanel.id = `workerPanel-${id}`;
            workerPanel.className = 'worker-panel';

            var workerInfoDiv = document.createElement('div');
            workerInfoDiv.className = 'worker-info';
            workerInfoDiv.innerHTML = `
                <p>Miner ID: ${id}</p>
                <p>Status: ${status}</p>
                <p>Total Mined Resources: ${totalMinedResources}</p>
                <p>Total Received Money ($): ${totalReceivedMoney}</p>
            `;

            workerPanel.appendChild(workerInfoDiv);
            document.querySelector('.container').appendChild(workerPanel);
        } else {
            // Актуализация на съществуващия панел с новата информация
            existingPanel.querySelector('.worker-info').innerHTML = `
                <p>Miner ID: ${id}</p>
                <p>Status: ${status}</p>
                <p>Total Mined Resources: ${totalMinedResources}</p>
                <p>Total Received Money ($): ${totalReceivedMoney}</p>
            `;
        }
    });

    addActionMessage('Workers data updated successfully.');
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

    var newlineElement = document.createElement('br');
    actionMonitor.appendChild(newlineElement);
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

function startTimer() {
    var seconds = 0;
    gameTimerInterval = setInterval(function() {
        seconds++;
        var formattedTime = new Date(seconds * 1000).toISOString().substr(11, 8);
        document.getElementById('gameTimer').innerText = formattedTime;
    }, 1000);
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

function removeMiner(workerId) {
    fetch(`/mining-game/workers/remove/${workerId}`, {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to remove miner.');
            }
            return response.text();
        })
        .then(data => {
            console.log(data);
        })
        .catch(error => {
            console.error('Error removing miner:', error);
        });
}
