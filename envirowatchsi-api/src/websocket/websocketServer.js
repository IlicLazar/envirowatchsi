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

  try {
    const message = JSON.stringify(event);

    wss.clients.forEach((client) => {
      if (client.readyState === WebSocket.OPEN) {
        try {
          client.send(message, (error) => {
            if (error) {
              console.error("[WebSocket] Error sending message to client:", error.message);
            }
          });
        } catch (error) {
          console.error("[WebSocket] Synchronous error sending message:", error.message);
        }
      }
    });
  } catch (error) {
    console.error("[WebSocket] Failed to stringify or broadcast event:", error.message);
  }
}

module.exports = {
  initWebSocket,
  broadcastEvent,
};