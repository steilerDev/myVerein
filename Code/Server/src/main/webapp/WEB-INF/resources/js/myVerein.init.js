/**
 * Document   : myVerein.init.js
 * Description:
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

var initAdminButton,
    initSettingsButton,
    initTutorialButton;

$(document).ready(function() {
    if(!$('#initAdminForm').data('bootstrapValidator')) {
        //Enable bootstrap validator
        $('#initAdminForm').bootstrapValidator({
            excluded: [':disabled', ':hidden', ':not(:visible)']
        }) //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submission function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                initAdminButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/init/superAdmin',
                    type: 'POST',
                    data: $(e.target).serialize(),
                    error: function (response) {
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        initAdminButton.stopAnimation(-1);
                    },
                    success: function (response) {
                        showMessage(response, 'success', 'icon_check');
                        initAdminButton.stopAnimation(0);
                        $('#initAdminBox').addClass('hidden');
                        $('#initTutorialBox').removeClass('hidden');
                        $('body').scrollTop(0);
                    }
                });
            });
    }

    if (!$('#initSettingsForm').data('bootstrapValidator')) {
        //Enable bootstrap validator
        $('#initSettingsForm').bootstrapValidator() //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submition function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                initSettingsButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/init/settings',
                    type: 'POST',
                    data: new FormData($('#initSettingsForm')[0]),
                    xhr: function() {
                        var xhr = new window.XMLHttpRequest();
                        //Upload progress
                        xhr.upload.addEventListener("progress", function(evt){
                            if (evt.lengthComputable) {
                                var percentComplete = evt.loaded / evt.total;
                                console.log(percentComplete);
                            }
                        }, false);
                        return xhr;
                    },
                    success: function (response) {
                        showMessage(response, 'success', 'icon_check');
                        initSettingsButton.stopAnimation(1);
                        $('#initAdminBox').removeClass("hidden");
                        $('#initSettingsBox').addClass("hidden");
                        $('#firstName').focus();
                        $('body').scrollTop(0);
                    },
                    error: function (response) {
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        initSettingsButton.stopAnimation(-1);
                    },
                    cache: false,
                    contentType: false,
                    processData: false
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
            window.location.reload(false);
        });
    }
});