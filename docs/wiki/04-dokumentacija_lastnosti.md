# 4. Dokumentacija izvedenih lastnosti

Ta stran dokumentira kljucne implementirane lastnosti projekta EnviroWatchSI. Opisi so zapisani v slogu zakljucka Jira/GitLab nalog: namen, nacin implementacije in nacin uporabe.

## 4.1 Backend REST API

**Namen implementacije:** Zagotoviti enotno vstopno tocko za podatke o meteorologiji, kakovosti zraka, hidrologiji, uporabnikih in virih podatkov.

**Nacin implementacije:** Backend je implementiran z Node.js in Express. Aplikacija v `src/app.js` registrira REST poti za `/api/meteo`, `/api/air-quality`, `/api/hydro`, `/api/auth` in `/api/data-sources`. Vstopna datoteka `index.js` vzpostavi MongoDB povezavo, Swagger dokumentacijo, HTTP streznik, WebSocket streznik in scheduler.

**Nacin uporabe:** Odjemalci posiljajo HTTP zahteve na `http://localhost:3000/api`. Bralne zahteve vracajo JSON sezname meritev, administrativne zahteve pa zahtevajo `Authorization: Bearer <token>`.

## 4.2 Avtentikacija in vloge

**Namen implementacije:** Lociti javni pregled podatkov od administrativnih operacij.

**Nacin implementacije:** API uporablja `bcryptjs` za zgoscenje gesel in `jsonwebtoken` za izdajo JWT zetonov. Endpoint `/api/auth/register` ustvari uporabnika, prvi registrirani uporabnik pa dobi vlogo `admin`. Endpoint `/api/auth/login` preveri email in geslo ter vrne token. Middleware `authenticate` preveri token, `requireAdmin` pa dovoli samo uporabnike z vlogo `admin`.

**Nacin uporabe:** Administrator se prijavi prek spletne strani `/login`. Frontend shrani token v `localStorage` in ga uporablja pri dostopu do administrativnih endpointov.

## 4.3 Upravljanje virov podatkov

**Namen implementacije:** Omogociti dodajanje in vzdrzevanje zunanjih XML virov brez sprememb programske kode.

**Nacin implementacije:** Model `DataSource` hrani ime vira, tip (`meteo`, `air-quality`, `hydro`), URL, aktivnost, interval osvezevanja, zadnji status in napako. `dataSourceController` podpira ustvarjanje, urejanje, brisanje, rocno sinhronizacijo in brisanje vseh meritev.

**Nacin uporabe:** Administrator na strani `/admin` izpolni obrazec za vir podatkov in ga shrani. Nato lahko vir rocno sinhronizira ali ga pusti aktivnega za periodicni scheduler.

## 4.4 Samodejna sinhronizacija XML podatkov

**Namen implementacije:** Sistem mora samodejno prenasati sveze okoljske podatke in jih shranjevati v podatkovno bazo.

**Nacin implementacije:** `schedulerService` vsakih 60 sekund preveri aktivne vire. Ce je od zadnje osvezitve minil nastavljen interval, prenese XML, izbere ustrezen parser glede na tip vira, preveri podvojene zapise po `measuredAt` in `stationId` oziroma `stationName`, nato nove meritve shrani v MongoDB.

**Nacin uporabe:** Administrator nastavi `refreshIntervalMinutes` za vir. Sistem samodejno skrbi za osvezevanje, zadnji status pa je viden v administrativnem seznamu virov.

## 4.5 Parserji in normalizacija podatkov

**Namen implementacije:** Pretvoriti razlicne XML strukture v enotne JavaScript objekte, ki jih lahko aplikacija prikaze in shrani.

**Nacin implementacije:** Datoteka `src/utils/xmlParser.js` vsebuje parserje za kakovost zraka, meteorologijo in hidrologijo. Parserji iz XML dokumenta izlusčijo podatke merilne postaje, koordinat, casa meritve in posameznih okoljskih metrik.

**Nacin uporabe:** Parserjev uporabnik ne uporablja neposredno. Uporabljajo se pri rocni ali samodejni sinhronizaciji vira podatkov.

## 4.6 Filtriranje podatkov

**Namen implementacije:** Uporabniku omogociti pregled relevantnih meritev namesto celotnega nabora podatkov.

**Nacin implementacije:** Frontend komponenta `Filters` omogoca datumske filtre ter geografski filter s sirino, dolzino in radijem. Backend uporablja filter utility in query parametre za omejitev rezultatov.

**Nacin uporabe:** Uporabnik na straneh za podatke ali zemljevid vnese zacetni datum, koncni datum ali lokacijski filter. Po spremembi filtra frontend ponovno pridobi podatke iz API-ja.

## 4.7 Interaktivni zemljevid

**Namen implementacije:** Meritve prikazati prostorsko, saj so okoljski podatki tesno povezani z lokacijo.

**Nacin implementacije:** Spletni vmesnik uporablja Leaflet in React Leaflet. Stran `/map` podpira preklop med plastmi meteorologije, kakovosti zraka in hidrologije. Za vsako postajo prikaze zadnjo meritev ali meritev za izbrani casovni trenutek.

**Nacin uporabe:** Uporabnik odpre stran **Zemljevid**, izbere tip podatkov, poisce postajo ali uporabi filtre. S klikom na merilno tocko pregleda podrobnosti.

## 4.8 Animacija podatkov skozi cas

**Namen implementacije:** Omogociti pregled razvoja okoljskih meritev skozi zaporedne casovne trenutke.

**Nacin implementacije:** Stran `/map` iz meritev izlusci unikatne vrednosti `measuredAt`, jih uredi in prikaze drsnik. Gumb za predvajanje premika casovni indeks in osvezuje prikazane postaje.

**Nacin uporabe:** Uporabnik na zemljevidu premika drsnik ali klikne predvajanje, da vidi spremembe meritev skozi cas.

## 4.9 Grafi, statistike in tabele

**Namen implementacije:** Podatke predstaviti na vec nacinov za hitrejse razumevanje.

**Nacin implementacije:** Spletni vmesnik uporablja komponente `StatsCard`, podatkovne tabele in Recharts grafe. Vsaka podatkovna stran prikazuje nabor meritev, osnovne statistike in graficni pregled izbranih vrednosti.

**Nacin uporabe:** Uporabnik odpre strani `/meteo`, `/air-quality` ali `/hydro` in pregleda podatke v tabeli, karticah in grafih.

## 4.10 Korelacijska analiza

**Namen implementacije:** Omogociti primerjavo med okoljskimi pojavi, na primer med padavinami in vodostajem.

**Nacin implementacije:** Stran `/correlations` nalozi podatke za meteorologijo, hidrologijo in kakovost zraka. Uporabnik izbere izvorni in ciljni nabor, metrike, postaje, radij in casovno okno. Frontend izracuna Pearsonovo korelacijo in prikaze interpretacijo ter grafe.

**Nacin uporabe:** Uporabnik izbere dve metriki, na primer `Padavine` in `Vodostaj`, ter pregleda izracunano povezanost.

## 4.11 WebSocket obvestila

**Namen implementacije:** Odjemalcem omogociti sprotno obvescanje o spremembah podatkov.

**Nacin implementacije:** Backend inicializira WebSocket streznik prek `websocketServer.js`. Pri ustvarjanju novih meritev ali posodobitvi vira podatkov controller oziroma scheduler poklice `broadcastEvent`.

**Nacin uporabe:** Frontend administrativna stran vzpostavi WebSocket povezavo in ob dogodku `DATA_SOURCE_UPDATED` posodobi prikaz vira.

## 4.12 Namizna aplikacija

**Namen implementacije:** Ponuditi alternativni desktop vmesnik za pregled in urejanje podatkov.

**Nacin implementacije:** Namizna aplikacija je implementirana s Kotlin Multiplatform in Compose Desktop. Skupni modul vsebuje modele, parserje in fetcherje, desktop modul pa zaslone za prijavo, pregled podatkov, vnos, posodabljanje in brisanje.

**Nacin uporabe:** Razvijalec aplikacijo zazene z `./gradlew :desktopApp:run`. Za ustvarjanje, posodabljanje in brisanje podatkov se uporabnik prijavi z admin racunom.

## 4.13 DSL in GeoJSON izvoz

**Namen implementacije:** Omogociti formalni opis okoljskih podatkov in njihovo pretvorbo v format, primeren za geografsko vizualizacijo.

**Nacin implementacije:** Modul `envirowatchsi-dsl` vsebuje lexer, parser, AST modele, semanticni validator in GeoJSON pretvornik. Testi preverjajo slovnico, semanticna pravila in serializacijo GeoJSON.

**Nacin uporabe:** Razvijalec pripravi `.irl` datoteko z okoljskimi stavki, zazene DSL modul in rezultat uporabi kot GeoJSON podatkovni sloj.

## 4.14 Dockerizacija

**Namen implementacije:** Poenostaviti zagon backenda in frontenda v enotnem okolju.

**Nacin implementacije:** Backend in frontend imata locena `Dockerfile`, korenski `docker-compose.yml` pa definira storitvi `backend` in `frontend`. Backend uporablja `.env` iz mape `envirowatchsi-api`.

**Nacin uporabe:** Iz korena projekta se zazene `docker compose up --build`, nato sta API in frontend dostopna na lokalnih portih 3000 in 5173.

