# Projekt aplikacji mobilnej - Lista zadań

Projekt aplikacji mobilnej stworzonej w ramach zajęć "Systemy wbudowane i mobilne" `01.2022`

Wyższa Szkoła Technologii Informatycznych w Katowicach - Wydział Informatyki - Kierunek Informatyka

* [Projekt aplikacji mobilnej - Lista zadań](#projekt-aplikacji-mobilnej---lista-zadań)
* [Opis aplikacji](#opis-aplikacji)
* [Funkcje aplikacji](#funkcje-aplikacji)
* [Wymagania](#wymagania)
* [Ekrany aplikacji](#ekrany-aplikacji)

## Opis aplikacji
Projekt aplikacji mobilnej „Lista zadań”, pracującej pod kontrolą systemu Android ma na celu wspomagać zarządzanie i planowanie zadań do zrobienia w określonym czasie.

Aby wspomóc szybkie manewrowanie po liście zadań, program poprzez wyraźne elementy graficzne, jak kolorową linią pod zadaniem, która symbolizuje priorytet ustalony przez użytkownika do zadania: zielony – niski, pomarańczowy – średni, czerwony – wysoki, w momencie, gdy dane zadanie, którego termin zakończenia jest mniejszy od aktualnej daty jest oznaczany na czerwono.

Aplikacja jest przystosowana do pracy w dwóch orientacjach, pionowym i poziomym, posiada możliwość pracy w trybie dziennym i nocnym.

Dodatkowo aplikacja wysyła użytkownikowi regularne powiadomienia z przypomnieniem o zadaniach, które są do wykonania w odstępach czasowych ustalonych przez użytkownika w ustawieniach aplikacji.

<p align="center">
  <img width="125" src="/images/icon.jpg" alt="Ikona - Lista Zadań">
</p>
Aplikacja do przechowywania ustawień używa mechanizmu preferencji, a do danych listy zadań SQLite przy pomocy klasy ROOM.

## Funkcje aplikacji
Funkcjonalności:
* wyświetlenie listy zadań z filtrowaniem na:
  * Wszystkie:
  * W trakcie:
  * Zrobione:
* dodanie zadania na listę,
* edycję zadania z listy,
* usuwanie zadania z listy,
* oznaczenie pojedynczego zadania jako zrobione lub w trakcie,
* generowanie przypomnienia o zadaniach do zrobienia, lub których termin minął, z możliwością wyłączenia takich powiadomień ogólnie, dla poszczególnych priorytetów oraz ustawianie częstotliwości powiadomień,
* import i eksport pojedynczego zadania do pliku tekstowego,
* import i eksport całej bazy danych z listą zadań do pliku .db,
* sortowanie zadań po co najmniej 3 kryteriach (np. po dacie dodania, terminie zakończenia zadania, priorytecie, nazwie zadania, statusie),
* nadanie zadaniom priorytetu
* tryb nocny i dzienny,
* wyszukiwanie zadania "na żywo" przy pomocy komponentu searchView,
* odświeżanie listy poprzez przycisk oraz poprzez "swipe" przeciągnięcie palcem z góry na dół,
* oznaczanie wszystkich zadań jako zrobionych lub w trakcie,
* resetowanie bazy danych
* kolorystyczne oznaczenie priorytetu zadania
* kolorystyczne oznaczenie przekroczenia końcowej daty z aktualną.


## Wymagania
Minimalna wersja systemu: Android 8.0 - `API 26`

## Ekrany aplikacji
| ![Okno startowe – filtr zadania w trakcie](/images/Okno%20startowe%20%E2%80%93%20filtr%20zadania%20w%20trakcie.jpg "Okno startowe – filtr zadania w trakcie") | ![Filtr zadanie zrobione](/images/Filtr%20zadanie%20zrobione.jpg "Filtr zadanie zrobione") | ![Filtr wszystkie zadania](/images/Filtr%20wszystkie%20zadania.jpg "Filtr wszystkie zadania") |
| :---: | :---: | :---: |
| *Okno startowe – filtr zadania w trakcie* | *Filtr zadanie zrobione* | *Filtr wszystkie zadania* |

| ![Wyszukiwanie „na żywo”](/images/Wyszukiwanie%20na%20%C5%BCywo.jpg "Wyszukiwanie „na żywo”") | ![Filtr w trakcie w trybie dziennym](/images/Filtr%20w%20trakcie%20w%20trybie%20dziennym.jpg "Filtr w trakcie w trybie dziennym") | ![Menu mobilne z lewej strony](/images/Menu%20mobilne%20z%20lewej%20strony.jpg "Filtr wszystkie zadania") |
| :---: | :---: | :---: |
| *Wyszukiwanie „na żywo”* | *Filtr w trakcie w trybie dziennym* | *Menu mobilne z lewej strony* |

| ![Menu główne (3 kropki)](/images/Menu%20g%C5%82%C3%B3wne%20-%203%20kropki.jpg "Menu główne (3 kropki)") | ![DialogBox - sortowanie](/images/DialogBox%20-%20sortowanie.jpg "DialogBox - sortowanie") | ![Okno o aplikacji](/images/Okno%20o%20aplikacji.jpg "Okno o aplikacji") |
| :---: | :---: | :---: |
| *Menu główne (3 kropki)* | *DialogBox - sortowanie* | *Okno o aplikacji* |

| ![DialogBox – nowe zadanie](/images/DialogBox%20%E2%80%93%20nowe%20zadanie.jpg "DialogBox – nowe zadanie") | ![DialogBox – edycja zadania](/images/DialogBox%20%E2%80%93%20edycja%20zadania.jpg "DialogBox – edycja zadania") | ![DialogBox – wyświetlanie zadania tryb nocny](/images/DialogBox%20%E2%80%93%20wy%C5%9Bwietlanie%20zadania%20tryb%20nocny.jpg "DialogBox – wyświetlanie zadania tryb nocny") | ![DialogBox – wyświetlanie zadania tryb dzienny](/images/DialogBox%20%E2%80%93%20wy%C5%9Bwietlanie%20zadania%20tryb%20dzienny.jpg "DialogBox – wyświetlanie zadania tryb dzienny") |
| :---: | :---: | :---: | :---: |
| *DialogBox – nowe zadanie* | *DialogBox – edycja zadania* | *DialogBox – wyświetlanie zadania tryb nocny* | *DialogBox – wyświetlanie zadania tryb dzienny* |

| ![Okno ustawień 1](/images/Okno%20ustawie%C5%84%201.jpg "Okno ustawień 1") | ![Okno ustawień 2](/images/Okno%20ustawie%C5%84%202.jpg "Okno ustawień 2") | ![Powiadomienie](/images/Powiadomienie.jpg "Powiadomienie") |
| :---: | :---: | :---: |
| *Okno ustawień 1* | *Okno ustawień 2* | *Powiadomienie* |