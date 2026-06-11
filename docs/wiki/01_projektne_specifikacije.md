# 1. Projektne specifikacije

Ta dokument opredeljuje namen, ciljne uporabnike, opis resitve in funkcionalne zahteve projekta EnviroWatchSI.

## 1.1 Namen projekta

Namen projekta EnviroWatchSI je vzpostaviti digitalni dvojcek okoljskega stanja v Sloveniji. Aplikacija na enem mestu zbira, shranjuje, filtrira in vizualizira okoljske meritve iz javnih virov, predvsem podatke ARSO za meteorologijo, kakovost zraka in hidrologijo.

Projekt naslavlja problem razdrobljenosti okoljskih podatkov. Uporabnik za osnovno oceno stanja okolja pogosto potrebuje vec razlicnih strani, tabel in virov. EnviroWatchSI te podatke zdruzi v enoten uporabniski vmesnik, kjer so meritve prikazane v tabelah, grafih, statistikah, korelacijskih analizah in na zemljevidu.

## 1.2 Skupine uporabnikov in potrebe

| Skupina uporabnikov | Potrebe |
|---|---|
| Splosna javnost | Hiter pregled lokalnega vremena, kakovosti zraka in stanja vodotokov za vsakodnevno nacrtovanje. |
| Sportniki in rekreativci | Informacije o temperaturi, padavinah, vetru, ozonu in delcih PM10/PM2.5 pred aktivnostjo na prostem. |
| Prebivalci ob vodotokih | Spremljanje trenutnega vodostaja in pretoka rek ter prepoznavanje morebitnih tveganih razmer. |
| Ekološki navdusenci in raziskovalci | Dostop do vec tipov meritev, casovnih filtrov, korelacij in primerjav med okoljskimi parametri. |
| Administratorji sistema | Upravljanje zunanjih XML virov, rocna sinhronizacija podatkov in nadzor stanja zajema podatkov. |

## 1.3 Opis resitve

EnviroWatchSI je sestavljen iz vec povezanih modulov:

| Modul | Opis |
|---|---|
| Backend API | Express REST API, ki skrbi za avtentikacijo, dostop do meritev, CRUD operacije, upravljanje virov podatkov in sinhronizacijo XML virov. |
| Podatkovna baza | MongoDB hrani uporabnike, vire podatkov in normalizirane meritve za zrak, vreme in vode. |
| Spletna aplikacija | React/Vite vmesnik za pregled podatkov, filtriranje, grafe, interaktivni zemljevid, korelacije in administracijo. |
| Namizna aplikacija | Kotlin Compose Desktop vmesnik za pregled in urejanje podatkovnih zapisov prek API-ja. |
| DSL modul | Kotlin jezikovni modul za opis okoljskih podatkov in izvoz v GeoJSON. |

Resitev deluje tako, da administrator v sistem doda zunanji XML vir podatkov. Scheduler na backendu periodicno preverja aktivne vire, prenese XML dokument, ga razcleni s parserjem, normalizira zapise in jih shrani v ustrezno MongoDB kolekcijo. Uporabniski vmesniki nato podatke pridobijo prek REST API-ja in jih prikazejo uporabniku.

## 1.4 Funkcionalne zahteve

| ID | Zahteva | Prioriteta | Status |
|---|---|---|---|
| F-01 | Sistem mora prikazati meteoroloske podatke, vkljucno s temperaturo, vlaznostjo, vetrom in padavinami. | Visoka | Implementirano |
| F-02 | Sistem mora prikazati podatke kakovosti zraka, vkljucno z AQI, PM10, PM2.5, O3, CO in SO2. | Visoka | Implementirano |
| F-03 | Sistem mora prikazati hidrološke podatke, vkljucno z vodostajem in pretokom. | Visoka | Implementirano |
| F-04 | Uporabnik mora lahko filtrirati podatke po datumu in geografskem obmocju. | Visoka | Implementirano |
| F-05 | Sistem mora prikazati merilne postaje na interaktivnem zemljevidu. | Visoka | Implementirano |
| F-06 | Sistem mora omogociti prikaz sprememb skozi cas z animacijskim drsnikom na zemljevidu. | Srednja | Implementirano |
| F-07 | Sistem mora omogociti prijavo administratorja z JWT zetonam. | Visoka | Implementirano |
| F-08 | Administrator mora lahko dodaja, ureja, brise in sinhronizira vire podatkov. | Visoka | Implementirano |
| F-09 | Sistem mora prek WebSocket povezave obvescati odjemalce o spremembah podatkov. | Srednja | Implementirano |
| F-10 | Sistem mora omogociti korelacijsko analizo med razlicnimi okoljskimi parametri. | Srednja | Implementirano |
| F-11 | API mora imeti dokumentacijo prek Swagger UI. | Srednja | Implementirano |
| F-12 | Projekt mora biti zagnan lokalno z ukazi npm/Gradle ali prek Docker Compose. | Visoka | Implementirano |

## 1.5 Nefunkcionalne in sistemske zahteve

| Kategorija | Zahteva |
|---|---|
| Dostopnost podatkov | Backend mora za bralne operacije vracati JSON odgovore prek REST endpointov. |
| Varnost | Administrativne operacije morajo zahtevati veljaven JWT in vlogo `admin`. |
| Zanesljivost | Scheduler mora pri napaki vira shraniti status napake in ne sme prekiniti delovanja celotnega API-ja. |
| Uporabnost | Podatki morajo biti razumljivo prikazani v tabelah, grafih, statistikah in zemljevidu. |
| Razsirljivost | Novi viri podatkov se dodajajo prek modela `DataSource`, brez spremembe uporabniskega vmesnika. |
| Razvojno okolje | Node.js, npm, MongoDB, Gradle, JDK in IntelliJ IDEA/VS Code. |

## 1.6 Podatkovni model

Ključni podatkovni objekti:

| Objekt | Namen |
|---|---|
| `User` | Hrani uporabnisko ime, email, zgosceno geslo in vlogo `admin` ali `viewer`. |
| `DataSource` | Hrani ime vira, tip podatkov, URL, aktivnost, interval osvezevanja in zadnji status sinhronizacije. |
| `Meteo` | Hrani meteoroloske meritve za merilne postaje. |
| `AirQuality` | Hrani meritve kakovosti zraka in koncentracije onesnazeval. |
| `Hydro` | Hrani hidrološke meritve vodostaja in pretoka. |

## 1.7 Omejitve in predpostavke

Sistem je odvisen od dostopnosti zunanjih ARSO XML virov in internetne povezave. Za lokalni zagon API-ja je potrebna delujoca MongoDB povezava. Spletni vmesnik v trenutni kodi privzeto uporablja oddaljeni API naslov, zato je za popolnoma lokalen zagon treba preveriti oziroma prilagoditi `baseURL` v datoteki `envirowatchsi-web/src/api/client/apiClient.js`.

