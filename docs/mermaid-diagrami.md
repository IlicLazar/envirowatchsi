# EnviroWatchSI - Mermaid diagrami za dokumentacijo

Ta datoteka vsebuje pripravljene Mermaid diagrame za koncno dokumentacijo projekta. Imena clanov skupine zamenjajte z dejanskimi imeni.

## Prva stran

```mermaid
gantt
    title EnviroWatchSI - Ganttov diagram projektnega dela
    dateFormat  YYYY-MM-DD
    axisFormat  %d.%m.%Y

    section Nacrtovanje
    Vzpostavitev repozitorija in strukture projekta :done, p1, 2026-05-01, 2026-05-05
    Analiza zahtev in izbira tehnologij           :done, p2, 2026-05-06, 2026-05-10

    section Razvoj jedra
    Backend API, MongoDB modeli in avtentikacija  :done, p3, 2026-05-11, 2026-05-24
    React spletni vmesnik                         :done, p4, 2026-05-18, 2026-06-03
    Kotlin desktop aplikacija                     :done, p5, 2026-05-24, 2026-06-08
    DSL jezik za okoljske podatke                 :done, p6, 2026-05-24, 2026-06-08

    section Integracija in DevOps
    Dockerizacija backend in frontend storitev    :done, p7, 2026-05-24, 2026-06-02
    GitHub Actions CI/CD in Azure webhook         :done, p8, 2026-05-24, 2026-06-05

    section Zakljucek
    Testiranje primerov uporabe                   :active, p9, 2026-06-05, 2026-06-12
    Koncna dokumentacija in zagovor               :p10, 2026-06-08, 2026-06-20
    Nadgradnje do konca 3. letnika                :p11, 2026-06-21, 2027-06-30
```

## Primer uporabe 1: pregled okoljskih meritev

```mermaid
sequenceDiagram
    actor Uporabnik
    participant Web as React spletna aplikacija
    participant API as Express API
    participant Cache as In-memory cache
    participant Mongo as MongoDB

    Uporabnik->>Web: Odpre stran Meteo/AirQuality/Hydro
    Web->>API: GET /api/meteo ali /api/air-quality ali /api/hydro
    API->>Cache: Preveri cache kljuc za filtre
    alt Podatki so v cache
        Cache-->>API: Vrne shranjene rezultate
    else Cache je prazen
        API->>Mongo: find(query).sort(measuredAt desc)
        Mongo-->>API: Meritve iz baze
        API->>Cache: Shrani rezultat
    end
    API-->>Web: JSON seznam meritev
    Web-->>Uporabnik: Prikaz tabele, grafa in statistike
```

## Primer uporabe 2: prijava administratorja in dodajanje meritve

```mermaid
sequenceDiagram
    actor Admin
    participant Web as React/Admin UI ali Desktop UI
    participant API as Express API
    participant Auth as JWT middleware
    participant Mongo as MongoDB
    participant WS as WebSocket server

    Admin->>Web: Vnese email in geslo
    Web->>API: POST /api/auth/login
    API->>Mongo: User.findOne(email)
    Mongo-->>API: Uporabnik z zgoscenim geslom
    API->>API: bcrypt.compare + jwt.sign
    API-->>Web: JWT token in vloga admin

    Admin->>Web: Doda novo meritev
    Web->>API: POST /api/air-quality z Bearer tokenom
    API->>Auth: authenticate()
    Auth->>Auth: jwt.verify()
    API->>Auth: requireAdmin()
    Auth-->>API: Dostop dovoljen
    API->>Mongo: AirQuality.create(record)
    Mongo-->>API: Ustvarjena meritev
    API->>WS: broadcastEvent(AIR_QUALITY_CREATED)
    API-->>Web: 201 Created
    WS-->>Web: Obvestilo o novi meritvi
    Web-->>Admin: Osvezen prikaz podatkov
```

## Primer uporabe 3: sinhronizacija zunanjega vira podatkov

```mermaid
sequenceDiagram
    participant Scheduler as Scheduler service
    participant Mongo as MongoDB
    participant Source as Zunanji XML vir
    participant Parser as XML parser
    participant WS as WebSocket server
    participant Client as Spletni/desktop klient

    Scheduler->>Mongo: DataSource.find({isActive: true})
    Mongo-->>Scheduler: Aktivni viri podatkov
    loop Vsak vir, ki je dosegel interval osvezitve
        Scheduler->>Source: fetch(source.url)
        Source-->>Scheduler: XML dokument
        Scheduler->>Parser: parseMeteoData/parseHydroData/parseAirQualityData
        Parser-->>Scheduler: Normalizirani zapisi postaj
        Scheduler->>Mongo: findOne(measuredAt + stationId)
        alt Zapis se ne obstaja
            Scheduler->>Mongo: Model.create(record)
            Scheduler->>WS: broadcastEvent(*_CREATED)
        else Zapis ze obstaja
            Scheduler->>Scheduler: Preskoci duplikat
        end
        Scheduler->>Mongo: Posodobi lastRefreshed/lastStatus
        Scheduler->>WS: broadcastEvent(DATA_SOURCE_UPDATED)
        WS-->>Client: Real-time obvestilo
    end
```

## Arhitektura programske resitve

```mermaid
flowchart LR
    subgraph Clients["Odjemalci"]
        Browser["React + Vite web aplikacija\nport 5173"]
        Desktop["Kotlin Compose Desktop"]
    end

    subgraph Backend["Backend storitev"]
        Express["Node.js + Express API\nHTTP REST port 3000"]
        WS["WebSocket server\nws prek istega HTTP streznika"]
        Scheduler["Scheduler service\nperiodicna sinhronizacija"]
        Swagger["Swagger UI\n/docs"]
        Auth["JWT auth middleware\nadmin/viewer"]
    end

    subgraph Data["Podatkovni sloj"]
        Mongo[("MongoDB\nMongoose modeli")]
        Cache["In-memory cache"]
    end

    subgraph External["Zunanji sistemi"]
        XmlSources["Zunanji XML viri\nmeteo, hidro, zrak"]
        DockerHub["Docker Hub"]
        Azure["Azure VM / deploy webhook\nport 8080"]
        GitHub["GitHub Actions"]
    end

    Browser -->|"HTTP/JSON REST"| Express
    Browser <-->|"WebSocket JSON"| WS
    Desktop -->|"HTTP/JSON REST"| Express
    Express --> Auth
    Express --> Cache
    Express --> Mongo
    Express --> Swagger
    Scheduler --> XmlSources
    Scheduler --> Mongo
    Scheduler --> WS
    GitHub -->|"docker build/push"| DockerHub
    GitHub -->|"POST /deploy"| Azure
    Azure -->|"pull/run images"| DockerHub
```

## Razredni diagram: backend JavaScript/Node.js

```mermaid
classDiagram
    class App {
        +use(cors)
        +use(express.json)
        +GET_health()
        +mountRoutes()
    }

    class AuthMiddleware {
        +authenticate(req,res,next)
        +requireAdmin(req,res,next)
    }

    class AuthController {
        +register(req,res)
        +login(req,res)
        -generateToken(user)
    }

    class AirQualityController {
        +getAllAirQuality(req,res)
        +getNearbyAirQuality(req,res)
        +createAirQuality(req,res)
        +updateAirQuality(req,res)
        +deleteAirQuality(req,res)
    }

    class MeteoController {
        +getAllMeteo(req,res)
        +getNearbyMeteo(req,res)
        +createMeteo(req,res)
        +updateMeteo(req,res)
        +deleteMeteo(req,res)
    }

    class HydroController {
        +getAllHydro(req,res)
        +getNearbyHydro(req,res)
        +createHydro(req,res)
        +updateHydro(req,res)
        +deleteHydro(req,res)
    }

    class DataSourceController {
        +getAllDataSources(req,res)
        +createDataSource(req,res)
        +updateDataSource(req,res)
        +deleteDataSource(req,res)
        +syncDataSource(req,res)
        +clearAllMeasurements(req,res)
    }

    class SchedulerService {
        +startScheduler()
        +syncSource(source)
        -checkAndSyncSources()
    }

    class WebSocketServer {
        +initWebSocket(server)
        +broadcastEvent(event)
    }

    class User {
        +String username
        +String email
        +String password
        +String role
        +Date createdAt
        +Date updatedAt
    }

    class AirQuality {
        +String stationId
        +String stationName
        +Number latitude
        +Number longitude
        +Date measuredAt
        +Number pm10
        +Number pm2_5
        +Number o3
        +Number co
        +Number so2
        +Number airQualityIndex
    }

    class Meteo {
        +String stationId
        +String stationName
        +Number latitude
        +Number longitude
        +Date measuredAt
        +Number temperature
        +Number humidity
        +Number windSpeed
        +String windDirection
        +Number precipitation
    }

    class Hydro {
        +String stationId
        +String stationName
        +Number latitude
        +Number longitude
        +String riverName
        +Date measuredAt
        +Number waterLevel
        +Number waterFlow
    }

    class DataSource {
        +String name
        +String type
        +String url
        +Boolean isActive
        +Number refreshIntervalMinutes
        +Date lastRefreshed
        +String lastStatus
        +String lastError
    }

    App --> AuthController
    App --> AirQualityController
    App --> MeteoController
    App --> HydroController
    App --> DataSourceController
    AirQualityController --> AuthMiddleware
    MeteoController --> AuthMiddleware
    HydroController --> AuthMiddleware
    DataSourceController --> AuthMiddleware
    AuthController --> User
    AirQualityController --> AirQuality
    MeteoController --> Meteo
    HydroController --> Hydro
    DataSourceController --> DataSource
    SchedulerService --> DataSource
    SchedulerService --> AirQuality
    SchedulerService --> Meteo
    SchedulerService --> Hydro
    SchedulerService --> WebSocketServer
    AirQualityController --> WebSocketServer
    MeteoController --> WebSocketServer
    HydroController --> WebSocketServer
```

## Razredni diagram: Kotlin desktop in DSL

```mermaid
classDiagram
    class ApiClient {
        -String BASE_URL
        -Int TIMEOUT_MS
        -String? sessionAdminToken
        +getRaw(path) String
        +login(email,password) LoginResult
        +logout()
        +postJson(path,jsonBody) String
        +putForm(path,formBody) String
        +delete(path) String
        -request(path,method,body,contentType,requiresAdminToken) String
    }

    class LoginResult {
        +String token
        +String? username
        +String? email
        +String? role
    }

    class AirQualityStation {
        +String stationId
        +String stationName
        +Double latitude
        +Double longitude
        +String measuredAt
        +Double? pm10
        +Double? pm2_5
        +Double? airQualityIndex
    }

    class MeteoStation {
        +String stationId
        +String stationName
        +Double? latitude
        +Double? longitude
        +String measuredAt
        +Double temperature
        +Double humidity
        +Double? windSpeed
    }

    class HydroStation {
        +String stationId
        +String stationName
        +Double? latitude
        +Double? longitude
        +String riverName
        +String measuredAt
        +Double? waterLevel
        +Double? waterFlow
    }

    class Parser {
        -List~Token~ tokens
        -Int current
        +parse() ProgramNode
    }

    class AstNode
    class ProgramNode {
        +List~CityNode~ cities
    }
    class CityNode {
        +String name
        +List~CityItemNode~ items
    }
    class StationNode
    class AirStationNode {
        +String name
        +PointNode location
        +List~AirItemNode~ items
    }
    class MeteoStationNode {
        +String name
        +PointNode location
        +List~MeteoItemNode~ items
    }
    class HydroStationNode {
        +String name
        +String river
        +PointNode location
        +List~HydroItemNode~ items
    }
    class PointNode {
        +String longitude
        +String latitude
    }

    ApiClient --> LoginResult
    ApiClient ..> AirQualityStation
    ApiClient ..> MeteoStation
    ApiClient ..> HydroStation
    Parser --> ProgramNode
    AstNode <|.. ProgramNode
    AstNode <|.. CityNode
    AstNode <|.. StationNode
    ProgramNode --> CityNode
    CityNode --> StationNode
    StationNode <|.. AirStationNode
    StationNode <|.. MeteoStationNode
    StationNode <|.. HydroStationNode
    AirStationNode --> PointNode
    MeteoStationNode --> PointNode
    HydroStationNode --> PointNode
```

## DevOps CI/CD potek

```mermaid
flowchart TD
    Dev["Razvijalec naredi push\nna main ali develop"] --> GitHub["GitHub webhook sprozi\nGitHub Actions workflow"]
    GitHub --> Checkout["Checkout repository\nactions/checkout@v4"]
    Checkout --> Login["Prijava v Docker Hub\nz GitHub secrets"]
    Login --> BuildBE["docker build backend\n./envirowatchsi-api"]
    BuildBE --> PushBE["docker push\nenvirowatchsi-backend:latest"]
    PushBE --> BuildFE["docker build frontend\n./envirowatchsi-web"]
    BuildFE --> PushFE["docker push\nenvirowatchsi-frontend:latest"]
    PushFE --> DeployHook["curl POST na Azure webhook\nhttp://68.210.201.189:8080/deploy"]
    DeployHook --> VerifySecret{"x-deploy-secret veljaven?"}
    VerifySecret -- "ne" --> Reject["Zavrni deploy"]
    VerifySecret -- "da" --> Pull["Azure streznik povlece\nnove Docker slike"]
    Pull --> Restart["Ponovni zagon backend\nin frontend containerjev"]
    Restart --> Live["Aplikacija je posodobljena\nAPI :3000, Web :5173"]
```

## Varnost programske resitve

```mermaid
flowchart TD
    Internet["Uporabnik / internet"] --> Firewall["Pozarni zid streznika"]
    Firewall -->|"dovoljeni porti: 3000, 5173, 8080"| Services["Docker storitve"]
    Firewall -->|"blokirani nepotrebni porti"| Blocked["Dostop zavrnjen"]

    Services --> API["Express API"]
    API --> PublicRoutes["Javne GET poti\npregled meritev"]
    API --> ProtectedRoutes["Zascitene POST/PUT/DELETE poti"]
    ProtectedRoutes --> Auth["JWT authenticate"]
    Auth --> RoleCheck["requireAdmin"]
    RoleCheck -->|"admin"| WriteOps["Ustvarjanje, urejanje, brisanje\nin sinhronizacija virov"]
    RoleCheck -->|"viewer ali brez vloge"| Deny["403 Access denied"]

    API --> Passwords["Gesla zgoscena z bcrypt"]
    API --> Secrets["JWT secret in deploy secret\nv .env/GitHub Secrets"]
    API --> Validation["Validacija vhodnih podatkov\nkoordinate, obvezna polja"]
    API --> Mongo["MongoDB podatki\nMongoose sheme in indeksi"]
```

## Tehnicni povzetek za besedilo ob diagramih

- Programska jezika: JavaScript za spletni vmesnik in backend, Kotlin za desktop aplikacijo in DSL.
- Podatkovna baza: MongoDB prek Mongoose ODM.
- Spletni streznik: Node.js HTTP server z Express aplikacijo.
- Protokoli: HTTP/REST za API, WebSocket za real-time obvestila, DNS/TCP/IP za povezovanje storitev, HTTP webhook za deploy.
- Vrata: 3000 za backend API, 5173 za frontend, 8080 za deploy webhook.
- Glavne knjiznice: React, Vite, Axios, Leaflet, Recharts, Express, Mongoose, bcryptjs, jsonwebtoken, ws, fast-xml-parser, Swagger UI, Kotlin Compose, Gson.
- DevOps: GitHub Actions, Docker, Docker Hub, Azure deploy webhook.
