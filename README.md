# PaysagesScriptKit

Scripts pour application Paysages

## Chargement des points par le service geoville (détection automatique du changement)

* Le shapefile doit contenir les attributs suivants qui correspondent aux attributs de l'OCS au temps t0:
  * CLEABS : identifiant du polygone dans lequel tombe le points
  * CODE_CS : code de la couverture du sol
  * CODE_US  : code de l'usage du sol

* L'attribut de changement de détection CHANGETYPE qui prend les valeurs:
  * 0 : chgt résidentiel
  * 1 : chgt industriel
  * 2 : chgt infrastructure
  * 3 : chgt autres


## Chargement des points pour saisir l'usage des zones baties

* Le shapefile doit contenir les attributs suivants:
  * nature
  * fonction
  

## Carrière en activité

## Usage agricole

## Zone en transition
