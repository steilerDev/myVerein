/**
 * Document   : myVerein.settings
 * Description: This JavaScript file contains all methods needed by the settings page.
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

var settingsSubmitButton,
    clubLogoDeleteButton,
    currentAdminSelectize,
    settingsFormBootstrapValidator,
    newCustomUserField,
    previousCreatedCustomUserFields;

function disableSettings() {
    //Disable the complete formllll
    settingsFormBootstrapValidator.find('input').prop("disabled", true);
    $('#clubLogoButton').addClass('btn-disabled');
    currentAdminSelectize[0].selectize.disable();

    //Hides settings that are only supposed to be edited by the super admin
    $('#superAdminSettings').addClass("hidden");

    //Re-enable fields that are open for everyone
    $('#adminPasswordNew').prop("disabled", false);
    $('#adminPasswordNewRe').prop("disabled", false);
    $('#currentAdminPassword').prop("disabled", false);
    $('#locale').prop("disabled", false);
}

function resetSettingsForm() {

    settingsFormBootstrapValidator.find('input').prop("disabled", false);

    $('.customUserField').remove();
    previousCreatedCustomUserFields = [];

    $('#clubLogoButton').removeClass('btn-disabled');

    settingsSubmitButton.enable();
    clubLogoDeleteButton.enable();

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

    currentAdminSelectize[0].selectize.enable();
    currentAdminSelectize[0].selectize.clear();

    //Reseting previous validation annotation
    settingsFormBootstrapValidator.data('bootstrapValidator').resetForm();
}

function clearPasswords() {
    $('#adminPasswordNew').val("");
    $('#adminPasswordNewRe').val("");
    $('#currentAdminPassword').val("");
}

function addCustomUserFieldSettings(name){
    name = name.trim();
    if(!(~$.inArray(name, previousCreatedCustomUserFields)) && name) {
        previousCreatedCustomUserFields.push(name);

        var newCustomUserField = '<div class="customUserField">' +
            '<input name="cuf_' + name + '" value="' + name + '" class="form-control" type="text"  />' +
            '<div class="checkbox">' +
            '<label>' +
            '<input type="checkbox" name="delete' + name + '" class="delete"> ' + getLocalizedString("deleteCustomField") + ' (' + name + ')</input>' +
            '</label>' +
            '</div>' +
            '<div class="checkbox">' +
            '<label>' +
            '<input type="checkbox" name="deleteContent' + name + '" class="deleteContent" disabled="disabled"> ' + getLocalizedString("deleteCustomFieldContent") + '</input>' +
            '</label>' +
            '</div>' +
            '<hr class="hr-small"/>' +
            '</div>';

        //Inserting HTML on top of the current field and catching new DOM element
        var newCustomUserFieldSet = $(newCustomUserField).insertBefore($('#newCustomUserField'));

        //Assigning listener for checkbox
        var currentDeleteContent = newCustomUserFieldSet.find($('.deleteContent'));
        newCustomUserFieldSet.find($('.delete')).change(function () {
            if ($(this).is(':checked')) {
                currentDeleteContent.prop("disabled", false);
            } else {
                currentDeleteContent.prop("disabled", true);
                currentDeleteContent.prop("checked", false);
            }
        });
    }
}

function loadSettings() {
    resetSettingsForm();
    settingsSubmitButton.startAnimation();
    $.ajax({
        url: '/api/admin/settings',
        type: 'GET',
        error: function (response) {
            settingsSubmitButton.stopAnimation(-1, function(button){
                button.disable();
            });
            //console.log(response.responseText);
        },
        success: function (response) {
            if(response.administrationNotAllowedMessage) {
                disableSettings();
                $('#currentAdminLabel').removeClass("hidden");
                showMessage(response.administrationNotAllowedMessage, 'warning', 'icon_error-triangle_alt');
            } else {
                $('#superAdminLabel').removeClass("hidden");
                $('#databaseHost').val(response.dbHost);
                $('#databasePort').val(response.dbPort);
                $('#databaseUser').val(response.dbUser);
                $('#databasePassword').val(response.dbPassword);
                $('#databaseCollection').val(response.dbName);
                $('#rememberMeTokenKey').val(response.rememberMeKey);
                $('#clubName').val(response.clubName);
                if(response.clubLogoAvailable) {
                    $('#clubLogoDelete').removeClass("hidden");
                } else {
                    $('#clubLogoDelete').addClass("hidden");
                }

                response.customUserFields.forEach(function(entry){
                    addCustomUserFieldSettings(entry);
                })
            }
            currentAdminSelectize[0].selectize.addItem(response.currentAdmin.email);
            $('#locale').val(locale);
            settingsSubmitButton.stopAnimation(1);
        }
    });
}

function loadSettingsPage(){
    if (!settingsSubmitButton) {
        //Enabling progress button
        settingsSubmitButton = new UIProgressButton(document.getElementById('settingsSubmitButton'));
    }

    if (!(settingsFormBootstrapValidator = $('#settingsForm')).data('bootstrapValidator')) {
        //Enable bootstrap validator
        settingsFormBootstrapValidator.bootstrapValidator() //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submition function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                settingsSubmitButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/api/admin/settings',
                    type: 'POST',
                    data: new FormData(settingsFormBootstrapValidator[0]),
                    error: function (response) {
                        settingsSubmitButton.stopAnimation(-1);
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                        clearPasswords();
                    },
                    success: function (response) {
                        settingsSubmitButton.stopAnimation(1);
                        showMessage(response, 'success', 'icon_check');
                        if(locale != $('#locale').val()) {
                            window.location.replace("/");
                        }
                        loadSettings();
                    },
                    cache: false,
                    contentType: false,
                    processData: false
                });
            });
    }

    if(!clubLogoDeleteButton) {
        //Enabling progress button
        clubLogoDeleteButton = new UIProgressButton(document.getElementById('clubLogoDelete'));
        $('#clubLogoDeleteButton').click(function(e){
            e.preventDefault();
            clubLogoDeleteButton.startAnimation();
            $.ajax({
                url: '/content/clubLogo',
                type: 'DELETE',
                error: function (response) {
                    clubLogoDeleteButton.stopAnimation(-1);
                    showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                },
                success: function (response) {
                    clubLogoDeleteButton.stopAnimation(0, function(button){
                        classie.add(button.el, 'hidden');
                    });
                    showMessage(response, 'success', 'icon_check');
                }
            });
        })
    }

    $('#clubLogo').change(function() {
        $('#clubLogoLabel').text($('#clubLogo').val());
    });

    if(!newCustomUserField) {
        (newCustomUserField = $("#newCustomUserField")).keydown(function(e){
            if (e.keyCode == 13) {
                e.preventDefault(); //Preventing enter on keydown
            }
        }).keyup(function (e) {
            //But loading new entry on keyup (since keeping enter pressed would fire the event multiple times)
            if (e.keyCode == 13) {
                if(newCustomUserField.val().length > 0) {
                    addCustomUserFieldSettings(newCustomUserField.val());
                    newCustomUserField.val('');
                }
            }
        }).blur(function () {
            if (newCustomUserField.val().length > 0) {
                addCustomUserFieldSettings(newCustomUserField.val());
                newCustomUserField.val('');
            }
        });
    }

    if (!(currentAdminSelectize = $('#currentAdmin'))[0].selectize) {
        //Enabling selection of super admin user from existing user
        currentAdminSelectize.selectize({
            persist: false,
            createOnBlur: true,
            create: false, //Not allowing the creation of user specific items
            hideSelected: true, //If an option is already in the list it is hidden
            preload: true, //Loading data immediately (if division is loaded without loading the available user, the added user gets removed because selectize thinks he is not valid)
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
                    url: '/api/admin/user',
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
            },
            onLoad: function() {
                loadSettings();
            }
        });
    } else
    {
        currentAdminSelectize[0].selectize.clearOptions();
        currentAdminSelectize[0].selectize.load(function (callback) {
            $.ajax({
                url: '/api/admin/user',
                type: 'GET',
                error: function () {
                    callback();
                },
                success: function (data) {
                    callback(data);
                }
            });
        });
    }
}