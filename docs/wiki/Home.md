# EnviroWatchSI - Wiki

## Digitalni dvojcek okoljskega stanja v Sloveniji

EnviroWatchSI je aplikacija za spremljanje okoljskega stanja v Sloveniji. Sistem zdruzuje podatke o vremenskih razmerah, kakovosti zraka in hidrološkem stanju voda ter jih prikazuje v spletni in namizni aplikaciji. Resitev uporablja javne ARSO XML vire, lastni backend API, podatkovno bazo MongoDB Atlas, interaktivne tabele, grafe, filtre, zemljevid in administrativni modul za upravljanje virov podatkov.

## Ekipa IRL

| Clan |
|---|
| Dejan Rankic |
| Nikolina Lukic |
| Lazar Ilic |

## Struktura dokumentacije

| Stran | Vsebina | Status |
|---|---|---|
| [1. Projektne specifikacije](./01_projektne_specifikacije.md) | Namen, ciljni uporabniki, opis resitve in funkcionalne zahteve | Zakljuceno |
| [2. Namestitev in prijava](./02_namestitev_in_prijava.md) | Navodila za lokalni zagon, Docker zagon in prvi dostop | Zakljuceno |
| [3. Primeri uporabe](./03_primeri_uporabe.md) | Pet kljucnih scenarijev uporabe z navodili | Zakljuceno |
| [4. Dokumentacija lastnosti](./04_dokumentacija_lastnosti.md) | Opis implementiranih funkcionalnosti in nacin uporabe | Zakljuceno |

## Javni podatkovni viri

| Vir | URL | Podatki |
|---|---|---|
| ARSO - kakovost zraka | `https://www.arso.gov.si/xml/zrak/ones_zrak_urni_podatki_zadnji.xml` | AQI, PM10, PM2.5, O3, CO, SO2 |
| ARSO - hidrološki podatki | `https://www.arso.gov.si/xml/vode/hidro_podatki_zadnji.xml` | vodostaj, pretok, merilne postaje |
| ARSO - meteorološki podatki | `https://meteo.arso.gov.si/uploads/probase/www/observ/surface/text/sl/observation_si_latest.xml` | temperatura, vlaga, veter, padavine |

## Tehnoloski pregled

| Del sistema | Tehnologije |
|---|---|
| Backend API | Node.js, Express, Mongoose, JWT, bcrypt, WebSocket, Swagger |
| Podatkovna baza | MongoDB |
| Spletni vmesnik | React, Vite, Axios, React Router, Leaflet, Recharts |
| Namizna aplikacija | Kotlin Multiplatform, Compose Desktop |
| DSL modul | Kotlin, Gradle, lexer, parser, semanticni validator, GeoJSON izvoz |
| DevOps | Docker, Docker Compose |

