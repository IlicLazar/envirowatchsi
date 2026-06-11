# 3. Primeri uporabe

Ta stran opisuje pet najpogostejsih nacinov uporabe aplikacije EnviroWatchSI. Scenariji so napisani kot kratka navodila od odprtja aplikacije do zakljucka naloge.

## 3.1 Preverjanje lokalne kakovosti zraka

**Kaj uporabnik zeli narediti:** Preveriti trenutno kakovost zraka za izbrano lokacijo ali najblizjo merilno postajo.

**Koraki izvedbe:**

1. Uporabnik odpre spletno aplikacijo na `http://localhost:5173`.
2. V navigaciji izbere stran **Kakovost zraka** ali odpre `/air-quality`.
3. Po potrebi uporabi datumski filter ali geografski filter z vnosom sirine, dolzine in radija.
4. V tabeli poisce zeljeno merilno postajo.
5. Pregleda vrednosti AQI, PM10, PM2.5, O3, CO in SO2.
6. Za prostorski pogled odpre se stran **Zemljevid** in izbere plast **Kakovost zraka**.

**Rezultat:** Uporabnik vidi trenutne oziroma filtrirane meritve kakovosti zraka za izbrano obmocje in lahko oceni, ali so razmere primerne za aktivnosti na prostem.

## 3.2 Spremljanje nevarnosti poplav

**Kaj uporabnik zeli narediti:** Preveriti vodostaj in pretok najblizje reke.

**Koraki izvedbe:**

1. Uporabnik odpre stran **Hidrologija** ali `/hydro`.
2. V tabeli pregleda merilne postaje in ime reke.
3. Po potrebi nastavi geografski filter za svoje obmocje.
4. Pregleda trenutni vodostaj v centimetrih in pretok v `m3/s`.
5. Odpre stran **Zemljevid** in izbere plast **Hidrologija**.
6. Klikne merilno tocko na zemljevidu za podrobnosti postaje.

**Rezultat:** Uporabnik dobi pregled trenutnega hidrološkega stanja v okolici in lahko spremlja spremembe pretoka ali vodostaja.

## 3.3 Nacrtovanje sportnih aktivnosti na prostem

**Kaj uporabnik zeli narediti:** Izbrati lokacijo in cas z ugodnimi vremenskimi pogoji ter sprejemljivo kakovostjo zraka.

**Koraki izvedbe:**

1. Uporabnik odpre stran **Meteorologija** ali `/meteo`.
2. Pregleda temperaturo, vlaznost, veter in padavine za izbrane postaje.
3. Nastavi datumski filter, ce zeli primerjati meritve v izbranem obdobju.
4. Odpre stran **Kakovost zraka** in preveri vrednosti O3, PM10 in PM2.5.
5. Za prostorsko primerjavo odpre **Zemljevid**, izbere meteorolosko ali zracno plast in pregleda merilne tocke.
6. Po potrebi uporabi animacijski drsnik za pregled sprememb skozi cas.

**Rezultat:** Uporabnik izbere primernejso lokacijo oziroma cas za tek, kolesarjenje ali drugo aktivnost na prostem.

## 3.4 Analiza vpliva padavin na vodostaj

**Kaj uporabnik zeli narediti:** Primerjati povezavo med padavinami in vodostajem oziroma pretokom rek.

**Koraki izvedbe:**

1. Uporabnik odpre stran **Korelacije** ali `/correlations`.
2. Kot izvorni nabor izbere **Meteorologija** in metriko **Padavine**.
3. Kot ciljni nabor izbere **Hidrologija** in metriko **Vodostaj** ali **Pretok**.
4. Izbere merilni postaji oziroma obmocje primerjave.
5. Nastavi radij primerjave in casovno okno.
6. Pregleda izracun korelacije, graf in interpretacijo povezave.

**Rezultat:** Uporabnik dobi vizualno in statisticno oceno, ali so padavine v izbranem obmocju povezane s spremembami vodostaja ali pretoka.

## 3.5 Administratorsko upravljanje virov podatkov

**Kaj uporabnik zeli narediti:** Dodati ali posodobiti zunanji XML vir ter sproziti sinhronizacijo podatkov.

**Koraki izvedbe:**

1. Administrator odpre stran `/login`.
2. Vnese email in geslo administratorskega racuna.
3. Po prijavi se odpre stran `/admin`.
4. V obrazec vnese ime vira, tip podatkov, URL vira, interval osvezevanja in oznaci, ali je vir aktiven.
5. Klikne **Dodaj vir** ali **Posodobi vir**.
6. V seznamu virov klikne sinhronizacijo izbranega vira.
7. Po koncu sinhronizacije pregleda status in stevilo novih meritev.

**Rezultat:** Sistem shrani vir podatkov, prenese XML dokument, ga razcleni in nove meritve shrani v MongoDB. Odjemalci prejmejo posodobitve prek WebSocket povezave.

