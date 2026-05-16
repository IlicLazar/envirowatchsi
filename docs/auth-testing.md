# JWT Authentication Testing

## Register User

```bash
curl -X POST http://localhost:8080/api/register \
-H "Content-Type: application/json" \
-d '{"username":"test","password":"test123"}'
```
Expected response:
```text
User registered
```
---
## Login User
```bash
curl -X POST http://localhost:8080/api/login \
-H "Content-Type: application/json" \
-d '{"username":"test","password":"test123"}'
```
Expected response:
```json
{"token":"<jwt-token>"}
```
---
## Access Protected Route Without Token
```bash
curl -X POST http://localhost:8080/api/air-quality \
-H "Content-Type: application/json" \
-d '{"stationName":"Test","latitude":46.0,"longitude":15.0,"aqi":50}'
```
Expected response:
```text
Unauthorized
```
---
## Access Protected Route With JWT Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/login \
-H "Content-Type: application/json" \
-d '{"username":"test","password":"test123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -X POST http://localhost:8080/api/air-quality \
-H "Content-Type: application/json" \
-H "Authorization: Bearer $TOKEN" \
-d '{"stationName":"Test","latitude":46.0,"longitude":15.0,"aqi":50}'
```
Expected response:
```text
Air quality record saved
```
---
# Tested Features 
- User registration
- User login
- JWT token generation
- JWT token validation
- Protected API routes
- Unauthorized access blocking
- Authorized POST request handling