/**
 * Document   : myVerein.settings
 * Description:
 * Copyright  : (c) 2015 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

var settingsSubmitButton;

function disableSettings() {
    $("#settingsForm :input").prop("disabled", true);
    $('#superAdmin')[0].selectize.disable();
    settingsSubmitButton.disable();
}

function loadSettings() {

}

function loadSettingsPage(){
    console.log("loading settings page");
    if (!$('#superAdmin')[0].selectize) {
        //Enabling selection of super admin user from existing user
        $('#superAdmin').selectize({
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
                    data: $(e.target).serialize(),
                    error: function (response) {
                        settingsSubmitButton.stopAnimation(-1);
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                    },
                    success: function (response) {
                        settingsSubmitButton.stopAnimation(1);
                        showMessage(response, 'success', 'icon_check');
                    }
                });
            });
    }

    $('#clubLogo').on('fileselect', function(event, numFiles, label) {
        console.log(numFiles);
        console.log(label);
        console.log($('#clubLogo').val());
    });

    loadSettings();
}