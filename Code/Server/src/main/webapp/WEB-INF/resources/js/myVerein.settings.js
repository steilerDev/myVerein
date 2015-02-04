/**
 * Document   : myVerein.settings
 * Description:
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

var settingsSubmitButton;

function disableSettings() {
    //Disable the complete form
    $("#settingsForm :input").prop("disabled", true);
    $('#clubLogoButton').addClass('btn-disabled');
    $('#currentAdmin')[0].selectize.disable();

    //Hides settings that are only supposed to be edited by the super admin
    $('#superAdminSettings').addClass("hidden");

    //Re-enable fields that are open for everyone
    $('#adminPasswordNew').prop("disabled", false);
    $('#adminPasswordNewRe').prop("disabled", false);
    $('#currentAdminPassword').prop("disabled", false);
    $('#locale').prop("disabled", false);
}

function resetSettingsForm() {

    $("#settingsForm :input").prop("disabled", false);
    $('#clubLogoButton').removeClass('btn-disabled');
    settingsSubmitButton.enable();
    $('#currentAdminLabel').addClass("hidden");
    $('#superAdminLabel').addClass("hidden");

    $('#superAdminSettings').removeClass("hidden");

    $('#databaseHost').val("");
    $('#databasePort').val("");
    $('#databaseUser').val("");
    $('#databasePassword').val("");
    $('#databaseCollection').val("");
    $('#rememberMeTokenKey').val("");
    $('#clubName').val("");
    $('#locale').val("default");
    clearPasswords();

    var currentAdmin = $('#currentAdmin')[0].selectize;
    currentAdmin.enable();
    currentAdmin.clear();

    //Reseting previous validation annotation
    $('#settingsForm').data('bootstrapValidator').resetForm();
}

function clearPasswords() {
    $('#adminPasswordNew').val("");
    $('#adminPasswordNewRe').val("");
    $('#currentAdminPassword').val("");
}

function loadSettings() {
    resetSettingsForm();
    settingsSubmitButton.startAnimation();
    //Sending JSON request with the division name as parameter to get the division details
    $.getJSON("/settings", function (settings) {
        if(settings)
        {
            if(settings.administrationNotAllowedMessage) {
                disableSettings();
                $('#currentAdminLabel').removeClass("hidden");
                showMessage(settings.administrationNotAllowedMessage, 'warning', 'icon_error-triangle_alt');
            } else {
                $('#superAdminLabel').removeClass("hidden");
                $('#databaseHost').val(settings.dbHost);
                $('#databasePort').val(settings.dbPort);
                $('#databaseUser').val(settings.dbUser);
                $('#databasePassword').val(settings.dbPassword);
                $('#databaseCollection').val(settings.dbName);
                $('#rememberMeTokenKey').val(settings.rememberMeKey);
                $('#clubName').val(settings.clubName);
            }
            $('#currentAdmin')[0].selectize.addItem(settings.currentAdmin.email);
            $('#locale').val(locale);
        }
        settingsSubmitButton.stopAnimation(0);
    })
}

function loadSettingsPage(){
    if (!$('#currentAdmin')[0].selectize) {
        //Enabling selection of super admin user from existing user
        $('#currentAdmin').selectize({
            persist: false,
            createOnBlur: true,
            create: false, //Not allowing the creation of user specific items
            hideSelected: true, //If an option is allready in the list it is hidden
            preload: true, //Loading data immidiately (if division is loaded without loading the available user, the added user gets removed because selectize thinks he is not valid)
            valueField: 'email',
            labelField: 'email',
            searchField: 'email',
            disable: true,
            maxItems: 1,
            render: {
                option: function (item, escape) {
                    return '<div>' +
                        '<span class="name">' + escape(item.firstName) + ' ' + escape(item.lastName) + ' </span>' +
                        '<span class="description">(' + escape(item.email) + ')</span>' +
                        '</div>';
                }
            },
            load: function (query, callback) {
                $.ajax({
                    url: '/user/getUser',
                    type: 'GET',
                    data: {
                        term: query
                    },
                    error: function () {
                        callback();
                    },
                    success: function (data) {
                        callback(data);
                    }
                });
            }
        });
    }

    if (!settingsSubmitButton) {
        //Enabling progress button
        settingsSubmitButton = new UIProgressButton(document.getElementById('settingsSubmitButton'));
    }

    if (!$('#settingsForm').data('bootstrapValidator')) {
        //Enable bootstrap validator
        $('#settingsForm').bootstrapValidator() //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submition function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                settingsSubmitButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/settings',
                    type: 'POST',
                    data: new FormData($('#settingsForm')[0]),
                    error: function (response) {
                        settingsSubmitButton.stopAnimation(-1);
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        clearPasswords();
                    },
                    success: function (response) {
                        settingsSubmitButton.stopAnimation(1);
                        showMessage(response, 'success', 'icon_check');
                        if(locale != $('#locale').val()) {
                            window.location.reload(false);
                        }
                        clearPasswords();
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

    loadSettings();
}