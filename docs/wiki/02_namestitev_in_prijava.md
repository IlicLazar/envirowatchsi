# 2. Namestitev in prijava

Ta stran opisuje, kako novi uporabnik ali razvijalec vzpostavi EnviroWatchSI in se prijavi v sistem.

## 2.1 Sistemske zahteve

| Zahteva | Priporocilo |
|---|---|
| Operacijski sistem | Windows, macOS ali Linux |
| Node.js | Node.js 20 ali novejsi |
| npm | Verzija, ki pride skupaj z Node.js |
| MongoDB | Lokalni MongoDB ali MongoDB Atlas povezava |
| JDK | JDK 17 za Kotlin/Gradle module |
| Docker | Docker in Docker Compose za kontejnerski zagon |
| IDE | IntelliJ IDEA za Kotlin module, VS Code ali WebStorm za frontend/backend |

## 2.2 Prenos kode

```bash
git clone https://github.com/IlicLazar/envirowatchsi.git
cd envirowatchsi
```

## 2.3 Konfiguracija backend API-ja

V mapi `envirowatchsi-api` ustvarite datoteko `.env`.

```env
MONGO_URI=mongodb://127.0.0.1:27017/envirowatchsi
JWT_SECRET=replace-with-development-secret
```

`MONGO_URI` je povezava do MongoDB baze. `JWT_SECRET` je skrivnost za podpisovanje JWT zetonov in mora biti v produkciji dovolj dolg, nepredvidljiv niz.

Opomba za Linux/Docker okolja: v kodi mora import modela uporabnika ustrezati imenu datoteke. Datoteka je `src/models/User.js`, zato mora biti import v auth controllerju zapisan skladno z velikimi crkami datotecnega sistema.

## 2.4 Lokalni zagon backenda

```bash
cd envirowatchsi-api
npm install
npm run dev
```

API je nato dostopen na:

```text
http://localhost:3000
```

Preverjanje delovanja:

```text
http://localhost:3000/health
```

Swagger dokumentacija:

```text
http://localhost:3000/docs
```

## 2.5 Lokalni zagon spletnega vmesnika

```bash
cd envirowatchsi-web
npm install
npm run dev
```

Spletna aplikacija je privzeto dostopna na:

```text
http://localhost:5173
```

Ce zelite, da frontend uporablja lokalni backend, preverite datoteko:

```text
envirowatchsi-web/src/api/client/apiClient.js
```

Za lokalni zagon nastavite:

```javascript
baseURL: "http://localhost:3000/api"
```

V trenutni verziji projekta je lahko nastavljen tudi oddaljeni API naslov:

```javascript
baseURL: "http://68.210.201.189:3000/api"
```

## 2.6 Zagon z Docker Compose

Iz korenske mape projekta:

```bash
docker compose up --build
```

Storitve:

| Storitev | Naslov |
|---|---|
| Backend API | `http://localhost:3000` |
| Frontend | `http://localhost:5173` |

Pred zagonom mora obstajati `envirowatchsi-api/.env`. Ce uporabljate lokalni MongoDB zunaj Dockerja, mora biti `MONGO_URI` nastavljen tako, da je dostopen iz kontejnerja.

## 2.7 Zagon namizne aplikacije

```bash
cd envirowatchsi-desktop
./gradlew :desktopApp:run
```

Za razvojni zagon z osvezevanjem:

```bash
./gradlew :desktopApp:hotRun --auto
```

Testi za deljeno logiko:

```bash
./gradlew :shared:jvmTest
```

## 2.8 Zagon DSL modula

```bash
cd envirowatchsi-dsl
./gradlew test
```

Primer pretvorbe DSL opisa v GeoJSON je pripravljen v mapi `envirowatchsi-dsl/examples`.

## 2.9 Prva prijava v sistem

Spletni vmesnik ima prijavno stran:

```text
http://localhost:5173/login
```

Prijava uporablja endpoint:

```text
POST /api/auth/login
```

V API-ju obstaja tudi registracija:

```text
POST /api/auth/register
```

Primer registracije prvega administratorja:

```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@envirowatch.si",
    "password": "admin123",
    "confirmPassword": "admin123"
  }'
```

Prvi registrirani uporabnik samodejno dobi vlogo `admin`. Vsi naslednji registrirani uporabniki dobijo vlogo `viewer`. Po prijavi frontend shrani JWT zeton v `localStorage` in ga uporablja za administrativne zahteve.

## 2.10 Prvi koraki po prijavi

1. Odprite `http://localhost:5173/login`.
2. Vnesite email in geslo administratorskega uporabnika.
3. Po uspesni prijavi se odpre stran `/admin`.
4. Dodajte vir podatkov z imenom, tipom, URL naslovom in intervalom osvezevanja.
5. Kliknite sinhronizacijo vira, da sistem prenese XML podatke in ustvari meritve v bazi.
6. Odprite strani `/meteo`, `/air-quality`, `/hydro`, `/map` ali `/correlations` za pregled rezultatov.

## 2.11 Odpravljanje tezav

| Tezava | Mozni vzrok | Resitev |
|---|---|---|
| API se ne zazene | Manjka `.env` ali `MONGO_URI` | Ustvarite `.env` in preverite MongoDB povezavo. |
| Prijava ne uspe | Uporabnik se ni registriran ali je geslo napacno | Registrirajte prvega uporabnika prek `/api/auth/register`. |
| Admin stran vrne napako 401/403 | JWT manjka, je potekel ali uporabnik ni admin | Ponovno se prijavite z administratorskim racunom. |
| Frontend ne prikaze podatkov | Frontend klice napacen API naslov | Preverite `baseURL` v `apiClient.js`. |
| Sinhronizacija vira ne uspe | Zunanji XML URL ni dostopen ali tip vira ne ustreza parserju | Preverite URL, tip vira in stanje internetne povezave. |
| Docker backend ne doseze MongoDB | MongoDB ni v istem omrezju ali je `MONGO_URI` napacen | Nastavite pravilen host v `MONGO_URI` ali dodajte MongoDB servis v Compose. |

