/**
 * Document   : _myVerein.internationalization.js
 * Description: This JavaScript file is used as a message resolver, reading the current locale and returns the appropriate message.
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

function getLocalizedString(stringIdentifier)
{
    switch(stringIdentifier) {
        case "mapsNotAvailable":
            switch(locale) {
                case "de":
                    return "Die GoogleMaps API konnte nicht geladen werden, die Karte wird deshalb deaktiviert";
                    break;
                case "en":
                    return "Unable to reach GoogleMaps API, disabling map.";
                    break;
                default :
                    return "Unable to reach GoogleMaps API, disabling map.";
                    break;
            }
            break;
        case "deleteCustomField":
            switch(locale) {
                case "de":
                    return "L\u00f6sche das benutzerdefinierte Feld";
                    break;
                case "en":
                    return "Delete custom field";
                    break;
                default :
                    return "Delete custom field";
                    break;
            }
            break;
        case "deleteCustomFieldContent":
            switch(locale) {
                case "de":
                    return "L\u00f6sche zus\u00e4tzlich alle gespeicherten Werte innerhalb der Nutzerprofile";
                    break;
                case "en":
                    return "Also delete content of field within user profiles";
                    break;
                default :
                    return "Also delete content of field within user profiles";
                    break;
            }
            break;
        default:
            switch(locale) {
                case "de":
                    return "Lokalisierte Zeichenkette konnte nicht gefunden werden";
                    break;
                case "en":
                    return "Unable to retrieve localized String";
                    break;
                default :
                    return "Unable to retrieve localized String";
                    break;
            }
            break;
    }
}

/*
 Ä = \u00c4
 ä = \u00e4
 Ö = \u00d6
 ö = \u00f6
 Ü = \u00dc
 ü = \u00fc
 ß = \u00df
 */