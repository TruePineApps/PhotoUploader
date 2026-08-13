# Privacybeleid voor Photo-Uploader

**Ingangsdatum:** 3 april 2026

---

### 1. Inleiding en identiteit

Dit privacybeleid beschrijft hoe **Marcel van Heerwaarden**, handelend onder de naam
**True Pine Apps** ("de Ontwikkelaar"), omgaat met persoonsgegevens binnen de applicatie
**Photo-Uploader** ("de App"). De App is een persoonlijk hulpmiddel om foto's te uploaden naar Google
Foto's en ze te organiseren in albums op basis van de lokale mappenstructuur.

Als ontwikkelaar gevestigd in Nederland zet ik mij in voor de bescherming van uw privacy,
in overeenstemming met de Algemene Verordening Gegevensbescherming (AVG). De Ontwikkelaar
is de verwerkingsverantwoordelijke voor de persoonsgegevens die in dit beleid worden
beschreven.

**Contact:**  
**Marcel van Heerwaarden**, handelend onder de naam **True Pine Apps**  
KvK: 98723316  
E-mail: marcel@truepineapps.com  
Project: https://github.com/truepineapps/photouploader  
Volledige contactgegevens: https://truepineapps.com/nl/imprint

---

### 2. Welke persoonsgegevens worden verzameld en waarom

De enige persoonsgegevens die de Ontwikkelaar rechtstreeks verzamelt, zijn uw
**Google-e-mailadres**.

Uw e-mailadres wordt verzameld voor de volgende doeleinden:

- Om u toe te voegen aan de geautoriseerde testerslijst in Google Cloud Console, wat vereist
  is om u te kunnen authenticeren en de App te gebruiken.
- Om contact met u op te nemen over de status van uw toegang (bijvoorbeeld als uw toegang
  wordt ingetrokken of bij meldingen over wijzigingen in het gegevensgebruik).

**Rechtsgrondslag:** De verwerking van uw e-mailadres is noodzakelijk om u toegang te
verlenen tot de App en de testdienst te beheren. De rechtsgrondslag is
[artikel 6, lid 1, onder b, AVG](https://gdpr-info.eu/art-6-gdpr/) — verwerking die
noodzakelijk is voor de uitvoering van een dienst op uw verzoek.

Wanneer de verwerking niet strikt noodzakelijk is voor de uitvoering van de dienst — zoals
het verzenden van meldingen over toegangswijzigingen — is de rechtsgrondslag het
gerechtvaardigd belang van de Ontwikkelaar bij het beheer van de toegang tot de App en het
waarborgen van de goede werking ervan
([artikel 6, lid 1, onder f, AVG](https://gdpr-info.eu/art-6-gdpr/)).

De Ontwikkelaar verzamelt **geen** wachtwoorden, betalingsgegevens, locatiegegevens of
andere persoonsgegevens.

---

### 3. Fotoverwerking

Wanneer u de App gebruikt, worden uw foto's lokaal op uw apparaat verwerkt. De App leest
uw fotobestanden uit uw lokale opslag, verwerkt ze in het geheugen en uploadt ze rechtstreeks
naar uw eigen Google Photos-account via de Google Photos API. Hierbij wordt ook alle ingesloten 
metadata, zoals EXIF-data en locatie-informatie, naar Google Photos overgedragen. Uw foto's worden 
op geen enkel moment opgeslagen op of doorgestuurd naar servers die onder beheer van de 
Ontwikkelaar staan.

U blijft de enige eigenaar van uw foto's. U behoudt volledige controle over uw foto's en
gegevens in Google Photos. U kunt uw foto's rechtstreeks beheren, bekijken en verwijderen
via Google Photos, conform het beleid van Google. De Ontwikkelaar heeft nooit toegang tot
de inhoud van uw foto's.

---

### 4. Google API-scopes en gebruik

De App vraagt toegang tot uw Google-account om de acties uit te voeren die nodig zijn voor
de kernfunctionaliteit. De volgende toegangsscopes worden gebruikt:

- **`photoslibrary.appendonly`** — Om foto's te uploaden en albums aan te maken in uw
  Google Photos-bibliotheek.
- **`photoslibrary.readonly.appcreateddata`** — Om albums en foto's bij te houden die door
  de App zijn aangemaakt, zodat dubbele uploads worden voorkomen.
- **`photoslibrary.edit.appcreateddata`** — Om albumomslagen in te stellen en albums te
  organiseren die door de App zijn aangemaakt.
- **`userinfo.profile`** — Om uw naam en profielfoto in de App-interface weer te geven,
  zodat u kunt bevestigen welk account actief is.
- **`userinfo.email`** — Om uw account te identificeren, uw autorisatie tijdens de testfase
  te verifiëren en onderscheid te maken tussen gebruikers met identieke weergavenamen.

Het gebruik en de overdracht van informatie ontvangen van Google API's door de App is in
overeenstemming met het
[Gebruiksbeleid voor Google API-services](https://developers.google.com/terms/api-services-user-data-policy),
inclusief de vereisten voor beperkt gebruik.

---

### 5. Google Cloud Platform, Google als verwerkingsverantwoordelijke en logboekregistratie

De App is geregistreerd op Google Cloud Platform (GCP), dat de Google API-inloggegevens van
de App beheert. GCP kan standaard administratieve logboeken bijhouden die betrekking hebben
op de werking van het project zelf, zoals configuratiewijzigingen. Deze logboeken bevatten
geen registraties van uw foto-uploads of uw gebruik van de App.

De Ontwikkelaar heeft er bewust voor gekozen om toegangslogboekregistratie in GCP niet in
te schakelen. Dit betekent dat er geen registratie wordt bijgehouden van wanneer u inlogt,
welke foto's u uploadt of hoe vaak u de App gebruikt. In combinatie met paragraaf 3 betekent
dit dat de Ontwikkelaar noch uw foto's noch enige registratie van uw activiteiten binnen de
App bewaart.

**Google treedt op als onafhankelijke verwerkingsverantwoordelijke** voor gegevens die via
haar eigen diensten worden verwerkt, waaronder OAuth-authenticatie, de Google Photos API en
Google Cloud Platform. Dit betekent dat Google de doeleinden en middelen voor de verwerking
van uw gegevens binnen haar eigen infrastructuur bepaalt, onafhankelijk van de Ontwikkelaar.
De Ontwikkelaar heeft geen controle over welke gegevens Google verzamelt tijdens
authenticatie of API-interacties en kan geen uitspraken doen over de verwerking door Google
namens de Ontwikkelaar.

Aanvullende gegevens — zoals authenticatiemetadata — kunnen door Google worden verwerkt als
onderdeel van het OAuth-proces. Raadpleeg voor volledige informatie over hoe Google met uw
gegevens omgaat het [Privacybeleid van Google](https://policies.google.com/privacy).

---

### 6. Gegevensoverdracht buiten de Europese Unie

Google LLC is gevestigd in de Verenigde Staten. Door de App te gebruiken, worden uw
persoonsgegevens (e-mailadres, authenticatiemetadata en foto-inhoud) door Google verwerkt
op servers die zich mogelijk buiten de Europese Economische Ruimte (EER) bevinden.

Google heeft passende waarborgen getroffen voor dergelijke overdrachten in overeenstemming
met de AVG, waaronder standaardcontractbepalingen (SCC's) zoals goedgekeurd door de
Europese Commissie. Raadpleeg voor meer informatie het
[Privacybeleid van Google](https://policies.google.com/privacy) en het
[Raamwerk voor gegevensoverdracht van Google](https://business.safety.google/gdpr/).

---

### 7. Bewaartermijnen

| Gegevens                                          | Bewaartermijn                                                               |
|---------------------------------------------------|-----------------------------------------------------------------------------|
| E-mailadres (actieve testerslijst)                | Bewaard gedurende uw actieve testperiode                                    |
| E-mailadres (na verwijdering van de testerslijst) | Verwijderd binnen **6 maanden** na verwijdering                             |
| OAuth-tokens                                      | Alleen lokaal opgeslagen op uw apparaat; de Ontwikkelaar heeft geen toegang |

Wanneer uw e-mailadres is verwijderd, worden er geen verdere gegevens bewaard door de
Ontwikkelaar.

---

### 8. Bijzondere bepalingen voor de testfase

Omdat de App momenteel de status "Testen" heeft in de Google Cloud Console, gelden de
volgende voorwaarden:

- **Gebruikerslimiet:** Google beperkt de testfase tot maximaal 100 gelijktijdige
  gebruikers.
- **Beheer van de testerslijst:** Om zoveel mogelijk mensen de kans te geven de App te gebruiken,
  wordt de lijst met geautoriseerde gebruikers handmatig beheerd. Als de limiet van 100 
  gebruikers is bereikt, kan de toegang van een gebruiker worden ingetrokken om ruimte te 
  maken voor nieuwe aanvragen.
- **Melding:** Als uw toegang wordt ingetrokken, ontvangt u een korte melding op uw
  Google-e-mailadres.
- **Nieuwe aanvraag:** U bent van harte welkom om op elk gewenst moment opnieuw toegang
  aan te vragen door contact op te nemen met de Ontwikkelaar.

---

### 9. Gegevensbeveiliging

- **Geen delen met derden:** Uw gegevens worden nooit verkocht, gedeeld met derden,
  gebruikt voor advertentiedoeleinden of gebruikt voor het trainen van AI-modellen.
- **Authenticatietokens:** OAuth-vernieuwingstokens worden veilig opgeslagen op uw lokale
  apparaat. De Ontwikkelaar heeft nooit toegang tot uw Google-wachtwoord of uw
  inlogtokens.
- **Communicatie en opslag:** Uw e-mailadres wordt gebruikt om uw testtoegang te beheren,
  u op de hoogte te stellen van wijzigingen in uw toegangsstatus, en wordt opgeslagen in
  Google Cloud Console als onderdeel van de testerslijst. Het wordt niet gebruikt voor
  marketingdoeleinden. Zie paragraaf 5 voor de rol van Google bij de opslag van deze
  gegevens.
- **Geautomatiseerde besluitvorming:** De App maakt geen gebruik van geautomatiseerde
  besluitvorming of profilering zoals gedefinieerd in
  [artikel 22 AVG](https://gdpr-info.eu/art-22-gdpr/).

---

### 10. Gegevensbeschermingseffectbeoordeling

De Ontwikkelaar heeft beoordeeld of een gegevensbeschermingseffectbeoordeling (GEB, of 
'data protection impact assessment', DPIA) vereist is op grond van 
[artikel 35 AVG](https://gdpr-info.eu/art-35-gdpr/).

Gezien het feit dat:

- de App foto's uitsluitend verwerkt op uw lokale apparaat,
- de Ontwikkelaar geen toegang heeft tot de inhoud van uw foto's, en
- de enige rechtstreeks verzamelde persoonsgegevens uw e-mailadres betreft, uitsluitend
  gebruikt voor het beheer van uw testtoegang,

wordt het niet waarschijnlijk geacht dat de verwerking een hoog risico voor uw rechten en
vrijheden met zich meebrengt. Er is dan ook geen formele DPIA uitgevoerd. Deze
beoordeling wordt herzien als de functionaliteit van de App wezenlijk wijzigt.

---

### 11. Uw rechten onder de AVG

Op grond van de AVG heeft u de volgende rechten met betrekking tot uw persoonsgegevens.
Neem contact op met de Ontwikkelaar via **marcel@truepineapps.com** om een van deze rechten
uit te oefenen.

- **Recht op inzage ([artikel 15](https://gdpr-info.eu/art-15-gdpr/)):** U kunt een kopie
  opvragen van de persoonsgegevens die over u worden bewaard. De Ontwikkelaar verstrekt uw
  e-mailadres en de datum waarop het is geregistreerd.

- **Recht op verwijdering ([artikel 17](https://gdpr-info.eu/art-17-gdpr/)):** U kunt
  verzoeken uw e-mailadres te verwijderen van de testerslijst. Houd er rekening mee dat
  verwijdering ook betekent dat u de App niet langer kunt gebruiken, omdat uw e-mailadres
  vereist is voor authenticatie.

- **Recht op beperking van de verwerking ([artikel 18](https://gdpr-info.eu/art-18-gdpr/)):**
  U kunt verzoeken de verwerking van uw gegevens te beperken. Omdat uw e-mailadres de
  enige bewaarde gegevens zijn en noodzakelijk is voor toegang tot de App, betekent
  beperking van de verwerking dat uw toegang tot de App wordt opgeschort voor de duur van
  de beperking.

- **Recht van bezwaar ([artikel 21](https://gdpr-info.eu/art-21-gdpr/)):** U kunt bezwaar
  maken tegen de verwerking van uw persoonsgegevens. Omdat uw e-mailadres noodzakelijk is
  voor de levering van de door u gevraagde dienst, leidt bezwaar tot beëindiging van uw
  toegang tot de App.

- **Recht om Google-toegang in te trekken:** U kunt de toegang van de App tot uw
  Google-account op elk moment intrekken via uw
  [Google-beveiligingsinstellingen](https://myaccount.google.com/permissions).

- **Recht om een klacht in te dienen:** U heeft het recht een klacht in te dienen bij de
  Autoriteit Persoonsgegevens via
  [autoriteitpersoonsgegevens.nl](https://www.autoriteitpersoonsgegevens.nl).

De Ontwikkelaar reageert op elk verzoek tot uitoefening van rechten binnen **één maand**
na ontvangst, zoals vereist door
[artikel 12, lid 3, AVG](https://gdpr-info.eu/art-12-gdpr/).

---

### 12. Wijzigingen in dit beleid

Als dit privacybeleid op een wezenlijke manier wordt gewijzigd, worden actieve testers
minimaal **14 dagen voor** de inwerkingtreding op de hoogte gesteld via de App en per
e-mail. Om meldingen in de App mogelijk te maken, neemt de App bij het opstarten contact
op met truepineapps.com, uitsluitend om te controleren of het privacybeleid is bijgewerkt.
Als de controle niet kan worden voltooid vanwege een netwerkprobleem, gaat de App normaal
verder en probeert het opnieuw bij de volgende keer opstarten. Bij dit verzoek worden geen
persoonsgegevens verzonden.

Het bijgewerkte beleid wordt ook gepubliceerd op
[truepineapps.com/photouploader](https://truepineapps.com/photouploader) met een herziene
ingangsdatum. Voortgezet gebruik van de App na de ingangsdatum geldt als aanvaarding van
het bijgewerkte beleid.

---

### 13. Contactgegevens

Voor vragen over dit beleid of om uw rechten uit te oefenen, kunt u contact opnemen met:

**Marcel van Heerwaarden**, handelend onder de naam **True Pine Apps**  
KvK: 98723316  
E-mail: marcel@truepineapps.com  
Project: https://github.com/truepineapps/photouploader  
Volledige contactgegevens: https://truepineapps.com/nl/imprint