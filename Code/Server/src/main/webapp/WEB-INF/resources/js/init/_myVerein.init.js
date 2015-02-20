/**
 * Document   : myVerein.init.js
 * Description: This JavaScript file contains all methods needed by the init page.
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

var initAdminButton,
    initSettingsButton,
    initTutorialButton,
    initSettingsFromBootstrapValidator,
    initAdminFromBootstrapValidator;

$(document).ready(function() {
    if(!(initAdminFromBootstrapValidator = $('#initAdminForm')).data('bootstrapValidator')) {
        //Enable bootstrap validator
        initAdminFromBootstrapValidator.bootstrapValidator({
            excluded: [':disabled', ':hidden', ':not(:visible)']
        }) //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submission function
                // Prevent form submission
                e.preventDefault();

                //Setting everything straight
                $('#subHeading').addClass('hidden');
                $('#subHeadingWait').removeClass('hidden');
                $('#initAdminBox').addClass('hidden');
                $('#initTutorialBox').removeClass('hidden');
                $('body').scrollTop(0);

               //Send the serialized form
                $.ajax({
                    url: '/api/init/superAdmin',
                    type: 'POST',
                    data: $(e.target).serialize(),
                    error: function (response) {
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        $('#initAdminBox').removeClass('hidden');
                    },
                    success: function (response) {
                        showMessage(response, 'success', 'icon_check');
                    }
                });
            });
    }

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
                    url: '/api/init/settings',
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
                            $('#initAdminBox').removeClass("hidden");
                            $('#initSettingsBox').addClass("hidden");
                            $('#firstName').focus();
                            $('body').scrollTop(0);
                        });
                    },
                    error: function (response) {
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        initSettingsButton.stopAnimation(-1);
                    }
                });
            });
    }

    $('#clubLogo').change(function() {
        $('#clubLogoLabel').text($('#clubLogo').val());
    });

    if(!initAdminButton) {
        //Enabling progress button
        initAdminButton = new UIProgressButton(document.getElementById('initAdminSubmitButton'));
    }

    if(!initSettingsButton) {
        //Enabling progress button
        initSettingsButton = new UIProgressButton(document.getElementById('initSettingsSubmitButton'));
    }

    if(!initTutorialButton) {
        //Enabling progress button
        initTutorialButton = new UIProgressButton(document.getElementById('initTutorialSubmit'));
        $('#initTutorialSubmitButton').click(function(e) {
            window.location.replace("/");
        });
    }
});