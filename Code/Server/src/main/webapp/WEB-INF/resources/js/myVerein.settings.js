/**
 * Document   : myVerein.settings
 * Description:
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
    //Disable the complete form
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
            '<input type="checkbox" name="delete' + name + '" class="delete"> Delete custom field (' + name + ')</input>' +
            '</label>' +
            '</div>' +
            '<div class="checkbox">' +
            '<label>' +
            '<input type="checkbox" name="deleteContent' + name + '" class="deleteContent" disabled="disabled"> Also delete content of field (' + name + ') within user profiles</input>' +
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
                if(settings.clubLogoAvailable) {
                    $('#clubLogoDelete').removeClass("hidden");
                } else {
                    $('#clubLogoDelete').addClass("hidden");
                }

                settings.customUserFields.split(",").forEach(function(entry){
                    addCustomUserFieldSettings(entry);
                })
            }
            currentAdminSelectize[0].selectize.addItem(settings.currentAdmin.email);
            $('#locale').val(locale);
            settingsSubmitButton.stopAnimation(1, function(button) {
                button.enable();
            });
        } else {
            settingsSubmitButton.stopAnimation(-1, function(button) {
                button.enable();
            });
        }
    })
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
                    url: '/settings',
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
                url: '/settings/deleteClubLogo',
                type: 'POST',
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
            },
            onLoad: function() {
                loadSettings();
            }
        });
    } else
    {
        currentAdminSelectize[0].selectize.load(function (callback) {
            $.ajax({
                url: '/user/getUser',
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