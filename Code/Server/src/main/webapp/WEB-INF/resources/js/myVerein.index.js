/**
 * Document   : myVerein.index.js
 * Description:
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

//This variable is holding the language code for the current locale.
var locale;

$(document).ready(function() {
    //Reading locale from cookie
    locale = $.cookie('myVereinLocaleCookie');
    if(!locale)
    {
        //Default locale is english
        locale = "en";
    }
    moment.locale(locale);

    //Defining callbacks, for each tab.
    var tabOptions = {
        onShow: function(currentTab){
            if(currentTab.id == "userTab")
            {
                loadUserPage();
            } else if(currentTab.id == "divisionTab")
            {
                loadDivisionPage();
            } else if(currentTab.id == "eventTab")
            {
                loadEventPage();
            }
        }
    }
    new CBPFWTabs(document.getElementById('tabs'), tabOptions);
});