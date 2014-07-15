'use strict';

var Page = require('./editUser');

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/users/:user?tab=account',

  myPassword: function() {
    return element(by.model('credentials.authenticatedUserPassword'));
  },

  newPassword: function() {
    return element(by.model('credentials.password'));
  },

  newPasswordRepeat: function() {
    return element(by.model('credentials.password2'));
  },

  changePasswordButton: function() {
    return element(by.css("button[type='submit']"));
  },

  deleteUserButton: function() {
    return element(by.css('.btn-danger'));
  },

  deleteUserAlert: function() {
    var ptor = protractor.getInstance();
    return ptor.switchTo().alert();
  }

});