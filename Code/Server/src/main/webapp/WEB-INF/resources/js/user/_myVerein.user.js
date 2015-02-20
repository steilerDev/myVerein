/**
 * Document   : myVerein.user
 * Description: This JavaScript file contains all methods needed by the user management page.
 * Copyright  : (c) 2014 Frank Steiler <frank@steilerdev.de>
 * License    : GNU General Public License v2.0
 */

var userSubmitButton,
    userDeleteButton,
    userList,
    userFormBootstrapValidator,
    userDivisionsSelectize,
    gender;

function addCustomUserFieldUser(key, value){
    var newCustomUserField =    '<div class="customUserField form-group">' +
                                    '<label for=cuf_' + key + '">' + key + '</label>' +
                                    '<input id="cuf_' + key + '" name="cuf_' + key + '" value="' + value + '" class="form-control"  type="text" placeholder="Enter the user\'s ' + key + '"/>' +
                                '</div>';
    $(newCustomUserField).insertAfter($('#customUserFieldsSeparator'));
}

//Reset the user form
function resetUserForm(doNotHideDeleteButton) {
    $('#firstName').val('');
    $('#lastName').val('');
    $('#email').val('');
    $('#password').val('');
    $('#birthday').val('');
    (gender = $("#gender")).val("default");

    $('#street').val('');
    $('#streetNumber').val('');
    $('#zip').val('');
    $('#city').val('');
    $('#country').val('');

    $('#activeMemberSince').val('');
    $('#passiveMemberSince').val('');
    $('#resignationDate').val('');

    $('#iban').val('');
    $('#bic').val('');

    //Removing created custom fields
    $('.customUserField').remove();
    $('#customUserFieldsError').addClass('hidden');
    $('#customUserFieldsEmpty').removeClass('hidden');

    //Hide & reset Password field
    $('#newUser').addClass("hidden");

    //Clear submit button
    $('#newUserButton').addClass('hidden');
    $('#oldUserButton').addClass('hidden');

    //Re-enable submit button
    userSubmitButton.enable();

    //Re-enable form
    userFormBootstrapValidator.find('input').prop("disabled", false);
    gender.prop("disabled", false);
    userDivisionsSelectize[0].selectize.enable();
    userDivisionsSelectize[0].selectize.clear();

    if(!doNotHideDeleteButton) {
        //Hide delete button
        $('#userDelete').addClass('hidden');
    }

    //Hide and clear heading
    $('#newUserHeading').addClass("hidden");
    $('#oldUserHeading').addClass("hidden");
    $('#oldUserHeadingName').empty();

    $('.privateUserInformation').removeClass('hidden');

    //Reseting previous validation annotation
    $('#userForm').data('bootstrapValidator').resetForm();
}

//Disabling the user form, if a user is not allowed to manipulate the user
function disableUserForm(){
    $('#userDelete').addClass('hidden');
    userFormBootstrapValidator.find('input').prop("disabled", true);
    gender.prop("disabled", true);
    $('.privateUserInformation').addClass('hidden');
    userDivisionsSelectize[0].selectize.disable();
    userSubmitButton.disable();
}

//Loading a user's information into the form
function loadUser(email) {
    userSubmitButton.startAnimation();
    //Sending JSON request with the email as parameter to get the user details
    $.ajax({
        url: '/api/admin/user',
        type: 'GET',
        data: {
            email: email
        },
        error: function (response) {
            userSubmitButton.stopAnimation(-1, function(button){
                button.disable();
            });
            //console.log(response.responseText);
        },
        success: function (user) {
            resetUserForm();
            var date;

            //Filling fields available for everyone
            $('#firstName').val(user.firstName);
            $('#lastName').val(user.lastName);
            $('#email').val(user.email);

            $("#gender").val(user.gender);
            $('#country').val(user.country);

            if(user.activeSince)
            {
                //Parsing date from response
                date = new Date(0);
                date.setUTCDate(user.activeSince.dayOfMonth);
                date.setUTCMonth(user.activeSince.monthValue - 1); //LocalDate is not a 0 starting index at the month
                date.setUTCFullYear(user.activeSince.year);
                $('#activeMemberSince').datepicker('setUTCDate', date);
            }

            if(user.passiveSince)
            {
                //Parsing date from response
                date = new Date(0);
                date.setUTCDate(user.passiveSince.dayOfMonth);
                date.setUTCMonth(user.passiveSince.monthValue - 1); //LocalDate is not a 0 starting index at the month
                date.setUTCFullYear(user.passiveSince.year);
                $('#passiveMemberSince').datepicker('setUTCDate', date);
            }

            if(user.resignationDate)
            {
                //Parsing date from response
                date = new Date(0);
                date.setUTCDate(user.resignationDate.dayOfMonth);
                date.setUTCMonth(user.resignationDate.monthValue - 1); //LocalDate is not a 0 starting index at the month
                date.setUTCFullYear(user.resignationDate.year);
                $('#resignationDate').datepicker('setUTCDate', date);
            }

            //Fill division list
            if (user.divisions) {
                $.each(user.divisions, function (index, division) {
                    userDivisionsSelectize[0].selectize.addItem(division.name);
                });
            }

            //Show important fields
            $('#oldUserHeading').removeClass("hidden");
            $('#oldUserHeadingName').text('<' + user.email + '>');
            $('#oldUserButton').removeClass('hidden');
            $('#userFlag').val(user.email);

            if(!user.administrationNotAllowedMessage) //No message means he is allowed to administrate, everything happening in this block shouldn't be part of the response anyway
            {
                $('#iban').val(user.iban);
                $('#bic').val(user.bic);
                $('#street').val(user.street);
                $('#streetNumber').val(user.streetNumber);
                $('#zip').val(user.zipCode);
                $('#city').val(user.city);

                $('#userDelete').removeClass('hidden');

                if(user.birthday)
                {
                    //Parsing date from response
                    date = new Date(0);
                    date.setUTCDate(user.birthday.dayOfMonth);
                    date.setUTCMonth(user.birthday.monthValue - 1); //LocalDate is not a 0 starting index at the month
                    date.setUTCFullYear(user.birthday.year);
                    $('#birthday').datepicker('setUTCDate', date);
                }

                //Inserting private information if there are any
                if (user.customUserField && Object.keys(user.customUserField).length > 0) {
                    console.log(user.customUserField);
                    console.log(Object.keys(user.customUserField).length);
                    $('#customUserFieldsEmpty').addClass('hidden');
                    $.each(user.customUserField, function (key, value) {
                        addCustomUserFieldUser(key, value);
                    });
                }

                userSubmitButton.stopAnimation(1);
            } else //If not allowed to edit disable some stuff and tell user
            {
                showMessage(user.administrationNotAllowedMessage, 'warning', 'icon_error-triangle_alt');
                disableUserForm();
                userSubmitButton.stopAnimation(1, function(button) {
                    button.disable();
                });
            }
        }
    });
}

//Set up everything to create a new user
function loadNewUser(doNotHideDeleteButton) {
    resetUserForm(doNotHideDeleteButton);
    $('#userFlag').val("true");
    $('#newUserHeading').removeClass('hidden');
    $('#newUser').removeClass('hidden');
    $('#newUserButton').removeClass('hidden');
    userSubmitButton.enable();
    $('#firstName').focus();

    $('#customUserFieldsEmpty').addClass('hidden');
    $('#customUserFieldsLoading').addClass('heartbeat');
    $.ajax({
        url: '/api/admin/settings/customUserFields',
        type: 'GET',
        error: function (response) {
            //console.log(response.responseText);
            $('#customUserFieldsError').removeClass('hidden');
            $('#customUserFieldsLoading').removeClass('heartbeat');
        },
        success: function (customUserFields) {
            if (customUserFields && customUserFields.length > 0) {
                $.each(customUserFields, function (index, customUserField) {
                    addCustomUserFieldUser(customUserField, "");
                });
            } else {
                $('#customUserFieldsEmpty').removeClass('hidden');
            }
            $('#customUserFieldsLoading').removeClass('heartbeat');
        }
    });
}

//(Re-)Load the user list on the left
function loadUserList() {
    userList.clear();
    $("#user-list-loading").addClass('heartbeat');
    //Loading user list through ajax request
    $.ajax({
        url: '/api/admin/user',
        type: 'GET',
        error: function (response) {
            $("#user-list-loading").removeClass('heartbeat');
            //console.log(response.responseText);
        },
        success: function (response) {
            $("#user-list-loading").removeClass('heartbeat');
            userList.add(response);
        }
    });
}

//This function is called as soon as the tab is shown. If necessary it is loading all required resources.
function loadUserPage() {
    if(!(userDivisionsSelectize = $('#divisions'))[0].selectize) {
        //Configuring division input field
        userDivisionsSelectize.selectize({
            persist: false,
            createOnBlur: true,
            create: false, //Not allowing the creation of user specific items
            hideSelected: true, //If an option is allready in the list it is hidden
            preload: true, //Loading data immidiately (if user is loaded without loading the available divisions, the added divisions get removed because selectize thinks they are not valid)
            valueField: 'name',
            labelField: 'name',
            searchField: 'name',
            load: function (query, callback) {
                $.ajax({
                    url: '/api/admin/division',
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
    } else
    {
        //Update entries within selectize list
        userDivisionsSelectize[0].selectize.clearOptions();
        userDivisionsSelectize[0].selectize.load(function (callback) {
            $.ajax({
                url: '/api/admin/division',
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

    if(!userList) {
        //Configuring fuzzy-search on user list.
        var listOptions = {
            valueNames: ['firstName', 'lastName', 'email'],
            item: '<li class="list-item"><h3><span class="firstName"></span> <span class="lastName"></span></h3><p class="email"></p></li>',
            plugins: [ListFuzzySearch()]
        };

        //Creating user list
        userList = new List('user-list', listOptions);

        //When items are added to the list the listener for the list items need to be updated.
        userList.on("updated", function () {
            $("li.list-item").click(function (e) {
                //Get the email as identification of the selected user and loading the user into the form
                loadUser($(this).children(".email").text());
            });
        });
    }

    if(!(userFormBootstrapValidator = $('#userForm')).data('bootstrapValidator')) {
        //Enable bootstrap validator
        userFormBootstrapValidator.bootstrapValidator({
            excluded: [':disabled', ':hidden', ':not(:visible)']
        }) //The constrains are configured within the HTML
            .on('success.form.bv', function (e) { //The submission function
                // Prevent form submission
                e.preventDefault();
                //Starting button animation
                userSubmitButton.startAnimation();
                //Send the serialized form
                $.ajax({
                    url: '/api/admin/user',
                    type: 'POST',
                    data: $(e.target).serialize(),
                    error: function (response) {
                        userSubmitButton.stopAnimation(-1);
                        showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                    },
                    success: function (response) {
                        userSubmitButton.stopAnimation(0);
                        $('#userFlag').val($('#email').val());
                        showMessage(response, 'success', 'icon_check');
                        loadUserList();
                    }
                });
            });
    }

    if(!userSubmitButton) {
        //Enabling progress button
        userSubmitButton = new UIProgressButton(document.getElementById('userSubmitButton'));
    }

    if(!userDeleteButton) {
        //Enabling progress button
        userDeleteButton = new UIProgressButton(document.getElementById('userDelete'));
        $('#userDeleteButton').click(function(e){
            e.preventDefault();
            userDeleteButton.startAnimation();
            $.ajax({
                url: '/api/admin/user?email=' + $('#userFlag').val(), //Workaround since DELETE request needs to be identified by the URI only and jQuery is not attaching the data to the URI, which leads to a Spring error.
                type: 'DELETE',
                //data: {
                //    email: $('#userFlag').val()
                //},
                error: function (response) {
                    userDeleteButton.stopAnimation(-1);
                    showMessage(response.responseText, 'error', 'icon_error-triangle_alt');
                },
                success: function (response) {
                    userDeleteButton.stopAnimation(1);
                    showMessage(response, 'success', 'icon_check');
                    loadNewUser(true);
                    loadUserList();
                }
            });
        })
    }

    if(!($('#activeMemberSince').data().datepicker && $('#birthday').data().datepicker && $('#passiveMemberSince').data().datepicker && $('#resignationDate').data().datepicker)) {
        //Global variables
        var datepickerOptions = {
            format: "dd/mm/yyyy",
            language: locale,
            todayHighlight: true
        };
        //Enable Datepicker
        $('#birthday').datepicker(datepickerOptions);
        $('#activeMemberSince').datepicker(datepickerOptions);
        $('#passiveMemberSince').datepicker(datepickerOptions);
        $('#resignationDate').datepicker(datepickerOptions);
    }


    $('#addUser').click(function (e) {
        loadNewUser();
    });

    loadUserList();
    loadNewUser();
}