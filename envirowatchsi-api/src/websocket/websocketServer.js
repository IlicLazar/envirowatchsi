const WebSocket = require("ws");

let wss;

function initWebSocket(server) {
  wss = new WebSocket.Server({ server });

  wss.on("connection", (socket) => {
    console.log("WebSocket client connected");

    socket.send(JSON.stringify({
      type: "CONNECTED",
      message: "Connected to EnviroWatchSI WebSocket server",
    }));

    socket.on("close", () => {
      console.log("WebSocket client disconnected");
    });
  });
}

function broadcastEvent(event) {
  if (!wss) return;

  const message = JSON.stringify(event);

  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}

module.exports = {
  initWebSocket,
  broadcastEvent,
};