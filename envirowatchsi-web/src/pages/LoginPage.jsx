import { useState } from "react";
import { login } from "../api/services/authService";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();

    try {
      const data = await login(email, password);

      localStorage.setItem("token", data.token);
      localStorage.setItem("user", JSON.stringify(data.user));

      setMessage(`Login successful. Role: ${data.user.role}`);
    } catch (error) {
      setMessage("Login failed. Check email and password.");
      console.error(error);
    }
  }

  return (
    <div className="auth-container">
      <h1>Admin Prijava</h1>
      <p style={{ color: "var(--text-secondary)", marginBottom: "24px", fontSize: "0.95rem" }}>
        Za dostop do administrativne nadzorne plošče
      </p>

      <form onSubmit={handleSubmit}>
        <div className="auth-form-group">
          <label className="filter-label">E-poštni naslov</label>
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            className="input-field"
            placeholder="vnesite e-pošto..."
          />
        </div>

        <div className="auth-form-group" style={{ marginBottom: "24px" }}>
          <label className="filter-label">Geslo</label>
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
            className="input-field"
            placeholder="vnesite geslo..."
          />
        </div>

        <button type="submit" className="btn btn-primary" style={{ width: "100%", padding: "12px" }}>
          Prijava
        </button>
      </form>

      {message && (
        <p
          style={{
            marginTop: "20px",
            color: message.includes("successful") ? "var(--accent-emerald)" : "var(--accent-red)",
            fontWeight: "500",
            fontSize: "0.95rem",
          }}
        >
          {message}
        </p>
      )}
    </div>
  );
}

export default LoginPage;