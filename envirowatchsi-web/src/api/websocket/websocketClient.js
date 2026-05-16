export function createWebSocketConnection(onMessage) {
    const socket = new WebSocket("ws://localhost:3000");
  
    socket.onopen = () => {
      console.log("WebSocket connected");
    };
  
    socket.onmessage = (event) => {
      const message = JSON.parse(event.data);
      onMessage(message);
    };
  
    socket.onerror = (error) => {
      console.error("WebSocket error:", error);
    };
  
    socket.onclose = () => {
      console.log("WebSocket disconnected");
    };
  
    return socket;
  }