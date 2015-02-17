/**
 * Document   : myVerein.base
 * Description: The JavaScript used by all pages.
 * Copyright  : (c) 2014 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

function showMessage (message, level, icon) {
	// create the notification
    var notification = new NotificationFx({
        wrapper : document.querySelector('.content-current'),
        message : '<span class="icon ' + icon + '"></span><p>' + message + '</p>',
        layout : 'attached',
        effect : 'bouncyflip',
        ttl : 7000,
        type : level, // notice, warning or error
    });

    // show the notification
    notification.show();
}