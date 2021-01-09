
## Lancement

$ cd sh
$ rocktest <fichier scen>

## Config

  Chaque personne a un environnement, schéma associé à un topic Kafka, associé à son prénom.
           Par variables d'environnement :

             - KAFKA_FUNC_TOPIC : nom du topic fonctionnel Kafka. Exemple : func_PRENOM
             - DATASOURCE_URL : URL JDBC pour la BDD. Exemple : jdbc:oracle:thin:@localhost:1521/SID
             - KAFKA_URI_PLAINTEXT : URI du broker Kafka. Exemple : localhost:9092
             - KAFKA_SCHEMA_REGISTRY_URL : URL pour le schema registry. Exemple : http://localhost:8080

## Lancement

  Lancer le shell rocktest
  Pour le lancer depuis Windows, il faut un bash, par exemple gitbash fonctionne très bien.

  ## Structure d'un fichier scénario

  ### Principe généraux

  Un scénario est un fichier au format yaml. Il contient des une liste de "steps", commençant par un "-" (en début de ligne, dans espace). Les propriétés du steps sont ensuite décalés de 2 espaces.
  `Attention :` L'indentation est très importante en YAML.

  #### Exemple

  ```yaml
           - type : title
             value : Inserer des donnees pour les tests de l'API Creance

           - type : display
             value : Test display
  ```
  Ce scénario dispose de 2 steps, de type respectivement "title" et "display" (voir ci dessous le détail des types).

           Chaque step contient les variables suivantes :

  * type : le type du step
  * value : le contenu du step. Sa signification dépend du type de step
  * _optionnel_ expect : vérifications à faire, le cas échéant

#### Variables

  Le champ _value_ peut contenir des appels à des variables, sous la forme

  ${VARIABLE}

  Lors de l'exécution, les variables sont remplacées.

           Exemple :

  ```yaml
           - type : display
             value : Valeur de la variable VAR = ${VAR}
  ```
  affiche le contenu de la variable d'environnement VAR.

  On peut aussi définir des variables dans le script, pour des ID de connexion par exemple, en utilisant un step _var_ (voir ci dessous).

  ### Step _var_

  Positionne une variable locale au scénario.

  ### Exemple

  ```yaml
           - type : var
             value : myvar = myvalue
  ```

  Créé la variable ${myvar} avec comme valeur "myvalue".

  #### Traitement

  Positionne une variable dans le contexte du scénario. On peut ensuite la référencer selon la syntase ${NOM_DE_VARIABLE}.

  #### Paramètres

  * type : var
  * value : NOM_DE_LA_VARIABLE = VALEUR_DE_LA_VARIABLE

#### Utilisation des variables

           On peut accéder aux variables selon 3 syntaxes :

  ```
  ${VARIABLE}
  ```

           La valeur est déterminée selon l'algorithme suivant:
  * Si VARIABLE est une variable d'environnement, elle est utilisée
  * Si VARIABLE est une variable dans le contexte du scénario, elle est utilisée
  * Sinon, aucune substitution n'est effectuée

  ```
  ${VARIABLE::valeur par defaut}
  ```

  * Si VARIABLE est une variable d'environnement, elle est utilisée
  * Si VARIABLE est une variable dans le contexte du scénario, elle est utilisée
  * Sinon, la valeur par défaut (après :: est retournée)

  ```
  ${VARIABLE?valeur si défini::valeur si non défini}
  ```

  * Si VARIABLE est définie, l'expression prend la valeur avant les ::
  * Sinon, elle prend la valeur après les ::

  *Remarque :* on peut utiliser des variables dans les valeurs retournées.

  *Exemple :*

  ```
  ${variable?Prefixe ${variable} suffixe::Autre prefixe ${anotherVariable} autre suffixe}
  ```

  * Si _variable_ est défini à "ma valeur", cette expression correspond à "Prefixe ma valeur suffixe".
  * Si _variable_ n'est pas définie, et que _anotherVariable_ est définie à "mon autre valeur", cette expression correspond à "Autre prefix mon autre valeur autre suffixe"

  ### Step _exit_

  Sort du scénario en cours.

  #### Exemple

  ```yaml
           - type : exit
  ```
  #### Traitement

  Arrête le scénario sans conditions. Utile en cours de test pour ne travailler que sur le début d'un scénario.

  #### Paramètres

  * type : exit

### Step _title_

#### Exemple

  ```yaml
           - type : title
             value : Inserer des donnees pour les tests de l'API Creance
  ```
  #### Traitement

  Permet de nommer le scénario, à titre informatif. Ce nom sera présent dans les traces ou les rapports de test. Il peut correspondre au cas de test HP-ALM par exmple.

  #### Paramètres

  * type : title
  * value : le titre du scénario

### Step _display_

#### Exemple

  ```yaml
           - type : display
             value : Message à afficher
  ```
  #### Traitement

  Permet d'afficher un message dans les logs.

  #### Paramètres

  * type : title
  * value : le message à afficher


### Step _request_

#### Exemple

  ```yaml
           - type : request
             value : DELETE FROM ${USER}."SP10"
  ```
  #### Traitement

  Effectue une requête SQL en écriture sur la base.

  Les paramètres de connexion à la base doivent être définis dans la variable d'environnement

  DATASOURCE_URL

           Exemple :

             jdbc:oracle:thin:@localhost:1521/SID

  se connecte sur l'instance SID de la base locale

  Si une erreur technique se produit pendant l'écriture, le scénario s'arrête automatiquement.

  #### Paramètres

  * type : request
  * value : la requête SQL.

  Il est recommandé d'utiliser une variable pour le schéma. Comme dans l'exemple, nous utilisont le schéma contenu dans la variable USER.
  Cet exemple vide donc la table "SP10" du schéma dont le nom est contenu dans la variable ${USER}

  ### Step _check_

  #### Exemple

  ```yaml
           - type : check
             value : SELECT COUNT(*) FROM ${USER}."CREANCE"
             expect :
               - 0
  ```
  #### Traitement

  Effectue une requête SQL en lecture et il est possible de vérifier le résultat.

  #### Paramètres

  * type : check
  * value : La requête SQL à exécuter
  * expect _optionnel_ : la liste des lignes à vérifier dans le retour.

#### Détails

  * Il est possible de vérifier que le retour contient plusieurs lignes. Il suffit de les renseigner à la suite, chacune commençant par quatre espaces et un tiret

           **Exemple** :

  ```yaml
           - type : check
             value : SELECT * FROM ${USER}."CREANCE"
             expect :
               - 1,2008-12-11 00:00:00,2008-12-11 00:00:00,2008-12-11 00:00:00,1,A2,2,01,ABCD,P,1,1
               - 2,2008-12-11 00:00:00,2008-12-11 00:00:00,2008-12-11 00:00:00,1,A2,2,01,ABCD,P,1,1
  ```

  L'ordre n'est pas important. Le test est OK si la sortie contient toutes les lignes.

  La valeur peut contenir une expression régulière, pour ignorer certains champs (comme par exemple des ID ou des dates)

           **Exemple** : pour ignorer le second champ, qui est une date pouvant bouger :

  ```yaml
           - type : check
             value : SELECT * FROM ${USER}."CREANCE"
             expect :
               - 1,.*,2008-12-11 00:00:00,2008-12-11 00:00:00,1,A2,2,01,ABCD,P,1,1
  ```

  `.*` correspond à une chaîne quelconque.

  * Il y a un mécanisme de retry. Si la vérification échoue, il va retenter un certain nombre de fois (chaque seconde pendant 30 secondes) afin de laisser le temps à un traitement de passer (ex: la synchro de la base). On n'a pas donc à se soucier de pause ou choses de ce genre, même si c'est quand même possible dans l'outil.

#### Enchainement de requêtes

  Il est possible de récupérer le résultat d'une requête de lecture pour l'utiliser dans une requête suivante, par exemple lire une table pour trouver une clé étrangère et interroger une seconde table liée.
           Pour cela, des variables numérotées sont automatiquement créées :

  * $0 : contient la dernière ligne retournée par la requête de lecture.
  * $1 ... $ _n_ : chaque champ du résultat (champ de la table). $1 = le premier champ, $2 = le second champ....

  `Attention :` seule la dernière ligne est retournée dans les variables. Il faut donc s'assurer que les requêtes retournent bien une seule ligne. Si elles en retournent plusieurs, seulement la dernière est poussée dans les variables $ _n_

  ### Step _pause_

  #### Exemple

  ```yaml
           - type : pause
             value : 2
  ```
  #### Traitement

  Fait une pause en secondes.

  #### Paramètres

  * type : pause
  * value : durée en seconde

### Step _http.get_

#### Exemple

  ```yaml
           - type : http.get
             value : ${API_IDRE_BASE_URL}/comptes/1/categories_ti
             expect :
               - code = 200
               - response.json[1].numeroRiba = numeroRiba1
  ```

  Cet exemple appelle le service IDRE sur une adresse contenue dans la variable d'environnement API_IDRE_BASE_URL.

  Puis il vérifie le résultat.

  #### Traitement

  Fait une requête GET sur une URL et vérifie le résultat (si il y a un paramètre expect, sinon, ne fait ausune vérification).

  #### Paramètres

  * type : http.get
  * value : L'URL à atteindre
  * expect : les conditions à vérifier

#### Détails

  Les vérifications peuvent porter sur le code retour ou le contenu du retour.

  * code = XXX : permet de vérifier que le code retour est XXX. Il est recommandé d'avoir toujours au moins cette vérification, afin que le scénario s'arrête en car de problème technique par exemple.
  * response.json<PATH> = XXX : permet de chercher dans le résultat un certain champ et de vérifier son contenu.

  *Exemple :*

           Si le résultat est de la forme suivante :

  ```json
  [
    {
      "numeroRiba": "numeroRiba0"
    },
    {
      "numeroRiba": "numeroRiba1"
    }
  ]
  ```
           L'expression suivante est correcte :

             - response.json[1].numeroRiba = numeroRiba1

  Le retour JSON est un tableau avec 2 entrée (numérotation commence à 0).

  "json[1].numeroRiba = numeroRiba1" correspondant au champ "numeroRiba" de la reconde entrée du tableau qui est bien "numeroRoba1".

           On peut aussi écrire des vérifications comme :

             - response.json[1].groupeProfessionnel.libelleGroupeProfessionnel = libelleGroupeProfessionnel

           Attention : si le retour n'est pas au format JSON (header http content-type="application/json"), la vérification échouera.

### Step _http.post_

#### Exemple

  ```yaml
           - type : title
             value : Post example

           - type : http.post
             value : http://localhost:8080/testing/backdoor/cibles
             body : '{
    "numeroCompteInterne":10000,
    "numerosCreances" : [1000000]
  }'
             expect :
               - code = 200
  ```
  #### Traitement

  Fait une requête POST sur une URL et vérifie le résultat (si il y a un paramètre expect, sinon, ne fait ausune vérification).

  #### Paramètres

  * type : http.post
  * value : L'URL à atteindre
  * expect : les conditions à vérifier
  * body : le contenu du body

#### Détails

  * Dans l'exemple précédent, le body est en JSON sur plusieurs lignes. Dans ce cas, il fait utiliser les simples quotes.
  * Les conditions *expect* sont similaires au GET.

  ### Step _http.put_

  #### Exemple

  ```yaml
           - type : http.put
             value : http://localhost:8080/testing/backdoor/cibles
             body : '{
    "numeroCompteInterne":10000,
    "numerosCreances" : [1000000]
  }'
             expect :
               - code = 200
  ```
  #### Traitement

  Fait une requête PUT sur une URL et vérifie le résultat (si il y a un paramètre expect, sinon, ne fait aucune vérification).

  #### Paramètres

  * type : http.put
  * value : L'URL à atteindre
  * expect : les conditions à vérifier
  * body : le contenu du body

#### Détails

  * Dans l'exemple précédent, le body est en JSON sur plusieurs lignes. Dans ce cas, il fait utiliser les simples quotes.
  * Les conditions *expect* sont similaires au GET.

  ### Step _http.delete_

  #### Exemple

  ```yaml
           - type : http.delete
             value: ${API_CREANCE_BASE_URL}/creances/1000002
             expect:
               - code = 204
  ```

  #### Traitement

  Fait une requête DELETE sur une URL et vérifie le résultat (si il y a un paramètre expect, sinon, ne fait aucune vérification).

  #### Paramètres

  * type : http.delete
  * value : L'URL à atteindre
  * expect : les conditions à vérifier

#### Détails

  * Les conditions *expect* sont similaires au GET.

  ### Step _call_

  Ce step permet de lancer un sous-scénario, afin d'initialiser des données par exemple.

  #### Exemple

  ```yaml
           - type : call
             value : module/lib.yaml
             params :
               param1 : value1
               param2 : value2
  ```

  #### Traitement

  Lance le scénario yaml localisé dans module/lib.yaml. Le chemin de base est celui premier scénario lancé. Il faut donc que le répertoire module qui contient lib.yaml soit dans le même réperetoire que le scénario principal.

  Un sous-scénario peut lui aussi lancer des sous-sous-scénarios. Le chemin de recherche est toujours l'emplacement du scénario principal.

  #### Paramètres

  * type : call
  * value : scénario à lancer
  * params : table des paramètres

           Les paramètres sont sous la forme similaire à l'exemple (param : value précédé de 4 espaces).
  A l'intérieur du script, les paramètres sont comme des variables dans le contexte du scénario, on les référence par la syntaxe ${nom_du_paramètre}.
  Les variables ne sont pas accessible en dehors du scénario cible (chaque scénario ou sous-scénario a son propre contexte).

  *Exemple :*

           Scénario appelant :

  ```yaml
           - type : call
             value : module/lib.yaml
             params :
               param1 : value1
  ```

           Scénario appelé :

  ```yaml
           - type : display
             value : Le paramètre vaut ${param1}
  ```

           Affiche :

  ```
  Le paramètre vaut value1
  ```

#### Détails

  *Exemple avec un scénario à 3 niveaux* :

  * libtest.yaml : il s'agit su scénario principal

  ```yaml
           - type : call
             value : module/lib.yaml
  ```

  * lib.yaml : sous scénario, localisé dans un répertoire module, au même endroit que libtest.yaml

  ```yaml
           - type : display
             value : Hello from lib !
           - type : call
             value : module/sublib.yaml
  ```

  Ce sous-scénario appelle lui-même un sous-sous scénario, dans le même répertoire "module"

  * sublib.yaml : sous-sous scénario, dans le même répertoire module

  ```yaml
           - type : display
             value : Hello from sublib !
  ```

           Si on exécute le scénatio libtest.yaml, on a le résultat suivant :

  ```

  03/12/2020 17:36:38.343 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - Start scenario. name=libtest.yaml, dir=.
  03/12/2020 17:36:38.479 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - [libtest] Step #1  : call,module/lib.yaml
  03/12/2020 17:36:38.480 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - Start scenario. name=./module/lib.yaml, dir=.
  03/12/2020 17:36:38.483 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - [libtest/lib] Step #1  : display,Hello from lib !
  03/12/2020 17:36:38.484 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - Hello from lib !
  03/12/2020 17:36:38.484 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - [libtest/lib] Step #2  : call,module/sublib.yaml
  03/12/2020 17:36:38.484 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - Start scenario. name=./module/sublib.yaml, dir=.
  03/12/2020 17:36:38.494 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - [libtest/lib/sublib] Step #1  : display,Hello from sublib !
  03/12/2020 17:36:38.494 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - Hello from sublib !
  03/12/2020 17:36:38.495 [INFO ] rocktest -[main]-  - f.a.r.r.r.Scenario - Scenario SUCCESS
  ```

  On constate que la pile d'appel est bien affichée à côté du numéro de step, ce qui permet de savoir où il y a eu une erreur en cas d'échec du scénario.

  * Retourner des variables : se fait depuis un sous-scénario en utilisant un step de type "return", voir ci-dessous.

### Step _return_

  Ce step permet à un sous-scénario de retourner des variables au scénario appelant.

  #### Exemple

  ```yaml
           - type : return
             value : variableRetour = valeurRetour
  ```

  #### Traitement

  Positionne une variable dans le contexte de l'appelant.

  On peut mettre plusieurs steps return afin de retourner plusieurs variables.

  #### Paramètres

  * type : return
  * value : VARIABLE = VALUE

#### Détails

           Pour récupérer la variable depuis l'appelant, il faut utiliser la syntaxe suivante :

             ${nom_du_sous_scenario.nom_de_la_variable}

  *Exemple :*

           Module module/sublib.yaml:

  ```yaml
           - type : return
             value : valeurRetour = Cette valeur vient de la lib
  ```

           Scénario appelant :

  ```yaml
           - type : call
             value : module/sublib

           - type: display
             value: Valeur de retour = ${sublib.valeurRetour}
  ```

           Le log d'exécution va contenir :

             Valeur de retour = Cette valeur vient de la lib

## Accès base de données

  On peut accéder à la base en appelant directement les requêtes SQL, par le biais des steps de type _request_ ou _check_.
  Pour faciliter l'appel, il est possible de générer des modules permettant d'accéder à une table.

  Pour cela, on décrit la table dans un fichier .desc dont la structure est définie ci-dessous. Ensuite, on utilise l'outil _createdb.sh_ afin de générer le module permettant d'accéder à la table.

### Description de la table

           Le fichier a la forme suivante :

  ```
  # Nom de l'objet métier
  name='creance'

  # Nom de la table
  table='${USER}."CREANCE"'

  # Définition des champs
  #   Nom métier
  #   Nom en base
  #   Type : N nombre, S chaine, D date
  #   Mandatory : M mandatory, O optionnel

  fields='
  id,ID_CREANCE,N,M
  dateCreation,DATE_CREATION,D,M
  dateMiseAJourOrigine,DATE_MISE_JOUR_ORIGINE,D,M
  periode,PERIODE,S,O
  sousPeriode,SOUS_PERIODE,S,O
  '
```

* name = le nom de l'entité métier
  * table = le nom de la table en base
  * fields = les champs de la table. Les caractéristiques des champs sont séparées par des virgules.
  * Champ1 : le nom métier du champ (celui qui sera utilisé dans les appels depuis un scénario YAML)
  * Champ2 : le nom du champ SQL en base
  * Champ3 : la catégorie de champ
      * N : numérique
      * S : chaîne de caractère
      * D : date (passée au format YYYYMMDD)
  * Champ4 : caractère obligatoire ou non
      * M : obligatoire (Mandatory)
      * O : optionnel

### Génération du module

           Appeler la commande suivante :

  ```
  rockdb <fichier .desc> <insert|delete|update>
  ```

  `Attention` : pour le moment, seul l'insert est disponible.

  Cette commande génère le module sur sa sortie standard. Il suffit de la rediriger dans un fichier.

           Exemple :

  ```shell
  rockdb creance.desc insert > creerCreance.yaml
  ```

  Génère un module "creerCreance.yaml" permettant d'insérer un objet en base.

  ```yaml
# Module pour objet creance
# YAML template :
# - type: call
#   value: lib/creerCreance
#   params:
#     id: <numero OBLIGATOIRE>
#     dateCreation: <date YYYYMMDD OBLIGATOIRE>
#     dateMiseAJourOrigine: <date YYYYMMDD OBLIGATOIRE>
#     dateMiseAJour: <date YYYYMMDD>
#     numeroCreance: <chaine OBLIGATOIRE>
           - type: checkParams
             values:
               - id
               - dateCreation
               - dateMiseAJourOrigine
               - numeroCreance

           - type: var
             value: localid = ${id::NULL}

           - type: var
             value: localdateCreation = ${dateCreation?TO_DATE('${dateCreation}', 'yyyymmdd')::NULL}

           - type: var
             value: localdateMiseAJourOrigine = ${dateMiseAJourOrigine?TO_DATE('${dateMiseAJourOrigine}', 'yyyymmdd')::NULL}

           - type: var
             value: localdateMiseAJour = ${dateMiseAJour?TO_DATE('${dateMiseAJour}', 'yyyymmdd')::NULL}

           - type: var
             value: localnumeroCreance = ${numeroCreance?'${numeroCreance}'::NULL}

           - type: request
             value: INSERT INTO ${USER}."CREANCE"(ID_CREANCE,DATE_CREATION,DATE_MISE_JOUR_ORIGINE,DATE_MISE_JOUR,NUMERO_CREANCE,CODE_MOTIF_ECART_NEGATIF,CODE_MODIFICATION_DEBIT,NUMERO_INTERNE_COMPTE,NUMERO_SEQUENTIEL,PERIODE,SOUS_PERIODE,MRI,MRC) VALUES (${localid},${localdateCreation},${localdateMiseAJourOrigine},${localdateMiseAJour},${localnumeroCreance},${localcodeMotifEcartNegatif},${localcodeModiticationDebit},${localnumeroInterneCompte},${localnumeroSequentiel},${localperiode},${localsousPeriode},${localmri},${localmrc})
  ```

  La zone de commmentaire au début contient un template d'appel. Les paramètres non marqués OBLIGATOIRE peuvent être omis.

           Exemple d'utilisation avec un seul champ optionnel positionné :

  ```yaml
           - type: call
             value: lib/creerCreance
             params:
               id: 1
               dateCreation: 20200102
               dateMiseAJourOrigine: 20180102
  ```

  ### Détails du *expect*

  Par défaut, chaque condition dans le *expect* doit être vraie.
           Il est possible de modifier ce comportement en utilisant des paramètres :
  * or - *optionnel* : il faut qu'au moins une des conditions soit vraie

           Exemple d'utilisation du *or* :

  (ici la seule condition vraie est le response.json)
  ```yaml
           type : http.post
             value : http://localhost:8080/testing/backdoor/cibles
             body: >-
               {
                 "numeroCompteInterne": 7148900,
                 "numerosCreances": [
                   1000000
                 ]
               }
             expect :
               - or:
                   - code = 205
                   - response.json['PROFIL_DETERMINE'].profilId = 8c8b3f6e-5795-4e8c-89cd-60d0be3a499d
                   - code = 202
  ```
  (ici la seule condition vraie est code = 200)
  ```yaml
           type : http.post
             value : http://localhost:8080/testing/backdoor/cibles
             body: >-
               {
                 "numeroCompteInterne": 7148900,
                 "numerosCreances": [
                   1000000
                 ]
               }
             expect :
               - code = 200
               - or:
                   - code = 205
                   - code = 200
  ```

