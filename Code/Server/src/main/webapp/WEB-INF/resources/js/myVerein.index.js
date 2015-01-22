/**
 * Document   : myVerein.index.js
 * Description:
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

$(document).ready(function() {
    //Defining callbacks, as soon as the page is loaded.
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