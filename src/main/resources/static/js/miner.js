var stompClient = null;

function connect() {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/workers', function(workers) {
            updatePageAndWorkers(JSON.parse(workers.body));
        });
    });
}

document.addEventListener('DOMContentLoaded', function() {
    connect();
});

function updatePageAndWorkers(data) {
    const message = data.message;
    const workers = data.workers;

    addActionMessage(message);

    if (workers && Array.isArray(workers)) {
        workers.forEach(worker => {
            const workerId = worker.id;
            const status = worker.status;
            const totalMinedResources = worker.totalMinedResources;
            const totalReceivedMoney = worker.totalReceivedMoney;

            const workerElement = document.getElementById(`worker-${workerId}`);
            if (workerElement) {
                const statusElement = workerElement.querySelector('.status');
                const minedResourcesElement = workerElement.querySelector('.totalMinedResources');
                const receivedMoneyElement = workerElement.querySelector('.totalReceivedMoney');

                if (statusElement && minedResourcesElement && receivedMoneyElement) {
                    statusElement.innerText = status;
                    minedResourcesElement.innerText = totalMinedResources;
                    receivedMoneyElement.innerText = totalReceivedMoney;
                } else {
                    console.error(`Worker ${workerId} elements not found.`);
                }
            } else {
                console.error(`Worker ${workerId} element not found.`);
            }
        });
    } else {
        console.error('Invalid workers data received.');
    }
}

function startGame(event) {
    event.preventDefault();

    const totalResourcesInput = document.getElementById('totalResources');
    const initialMinersInput = document.getElementById('initialMiners');

    const totalResources = parseInt(totalResourcesInput.value);
    const initialMiners = parseInt(initialMinersInput.value);

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
            updatePageAndWorkers(data);
        })
        .catch(error => {
            console.error('Error starting game:', error);
        });
}

function stopGame() {
    fetch('/stop', {
        method: 'POST'
    })
        .then(response => {
            if (response.ok) {
                console.log('Game stopped!');
            } else {
                throw new Error('Failed to stop game.');
            }
        })
        .catch(error => {
            console.error('Error stopping game:', error);
        });
}

function addActionMessage(message) {
    var actionMonitor = document.getElementById('actionMonitor');
    if (actionMonitor) {
        var messageElement = document.createElement('p');
        messageElement.textContent = message;
        actionMonitor.appendChild(messageElement);
    } else {
        console.error('Action monitor element not found.');
    }
}
