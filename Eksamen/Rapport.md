# Rapport - PGR112 Eksamen Juni 2025

## Innkapsling

I løsningen min har jeg brukt innkapsling gjennom å gjøre alle attributter i klassene private. For eksempel i Vehicle-klassen har jeg gjort alle felt som `id`, `brand`, `model` osv. private, og laget offentlige getter-metoder for å få tilgang til dem. Dette sikrer at data ikke kan endres direkte utenfra, som er et viktig prinsipp i objektorientert programmering. I Scrapyard-klassen har jeg også lagt til setter-metoder siden denne typen data kan trenge å bli oppdatert, mens Vehicle-dataene er finale og kan ikke endres etter objektet er opprettet.

Innkapsling hjelper meg å kontrollere hvordan data brukes og gir bedre sikkerhet i koden. Hvis jeg for eksempel senere vil legge til validering av data, kan jeg gjøre det i setter-metodene uten å påvirke resten av koden.

## Arv og Polymorfi

Jeg har implementert arv ved å lage en abstrakt superklasse `Vehicle` som inneholder alle felles egenskaper for kjøretøy. De tre subklassene `FossilCar`, `ElectricCar` og `Motorcycle` arver fra denne og legger til sine spesifikke egenskaper. For eksempel har `FossilCar` egenskaper som `fuelType` og `fuelAmount`, mens `ElectricCar` har `batteryCapacity` og `chargeLevel`.

Polymorfi brukes når jeg håndterer en liste av `Vehicle`-objekter i ScrapyardService-klassen. Jeg kan behandle alle kjøretøyene likt gjennom superklassen, men når jeg trenger spesifikk informasjon bruker jeg `instanceof` for å sjekke hvilken type det er. Den abstrakte metoden `getVehicleType()` blir overskrevet i hver subklasse, som er et godt eksempel på polymorfi i praksis.

## Utfordringer underveis

Den største utfordringen var å få til riktig mapping mellom objektene og databasen. Først brukte jeg record-klasser, men innså at dette ikke passet så godt med den objektorienterte tilnærmingen vi har lært. Jeg endret derfor til vanlige klasser med getters og setters.

En annen utfordring var å sikre trygg databasehåndtering. Først brukte jeg String.format() for SQL-spørringene, men lærte at dette kan være farlig på grunn av SQL injection. Jeg endret derfor til PreparedStatement som er mye tryggere. Koden ble lengre, men mye bedre.

Jeg er fornøyd med hvordan jeg løste fillesingen og databaseimporteringen. Koden leser strukturert gjennom vehicles.txt-filen og mapper dataene riktig til de forskjellige kjøretøytypene før de legges inn i databasen. Jeg delte også opp ScrapyardService i mindre metoder for å gjøre koden lettere å forstå.