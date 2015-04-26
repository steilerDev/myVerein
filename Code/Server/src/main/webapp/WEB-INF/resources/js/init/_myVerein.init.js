/**
 * Document   : myVerein.init.js
 * Description: This JavaScript file contains all methods needed by the init page.
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

var initSettingsButton,
    initTutorialButton,
    initSettingsFromBootstrapValidator;

$(document).ready(function() {
    if (!(initSettingsFromBootstrapValidator = $('#initSettingsForm')).data('bootstrapValidator')) {
        //Enable bootstrap validator
        initSettingsFromBootstrapValidator.bootstrapValidator() //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submition function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                initSettingsButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/api/init',
                    type: 'POST',
                    data: $(e.target).serialize(),
                    //Todo: Maybe later everywhere
                    //xhr: function() {
                    //    var xhr = new window.XMLHttpRequest();
                    //    //Upload progress
                    //    xhr.upload.addEventListener("progress", function(evt){
                    //        if (evt.lengthComputable) {
                    //            var percentComplete = evt.loaded / evt.total;
                    //            console.log(percentComplete);
                    //        }
                    //    }, false);
                    //    return xhr;
                    //},
                    success: function (response) {
                        showMessage(response, 'success', 'icon_check');
                        initSettingsButton.stopAnimation(1, function(){
                            // Todo: Redirect
                        });
                    },
                    error: function (response) {
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        initSettingsButton.stopAnimation(-1);
                    }
                });
            });
    }

    if(!initSettingsButton) {
        //Enabling progress button
        initSettingsButton = new UIProgressButton(document.getElementById('initSettingsSubmitButton'));
    }
});