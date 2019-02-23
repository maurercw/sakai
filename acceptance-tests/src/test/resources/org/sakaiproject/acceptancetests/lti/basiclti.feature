@Cwm
@Automated
Feature: LTI 1.1 Test Conditions

	Tests that a user can login

Background:
#    Given I am using "https://trunk-oracle.nightly.sakaiproject.org/portal"
    Given I am using "https://trunk-mysql.nightly.sakaiproject.org/portal"

@skip
@logout
Scenario Outline: 0.1 Site Setup
    Given I am logged in as "admin" with "admin"
    When I create a new site "<site_name>"
    Then Site created with name "<site_name>"
    And User "instructor2" added to site "<site_name>"  as role "maintain"
    And User "student0001" added to site "<site_name>"  as role "access"
    Examples:
        |site_name      |
        |LTI AFT Test 1 |

@skip
@logout
Scenario Outline: 1.0 Basic Functionality
    Given I am logged in as "<username>" with "<password>"
	And I'm in the "<tool_name>" tool of "<site_name>" site
	Then I "<status>" see an "Edit" button
    And The text "This tool has not yet been configured." displays
	Examples:
	    |tool_name      |site_name      |username       |password   |status     |
	    |External Tool |LTI AFT Test 1 |student0001    |sakai      |should not |
	    |External Tool |LTI AFT Test 1 |instructor2    |sakai      |should     |

@skip
@logout
Scenario: 2.0.10 Configuration Screen
    Given I am logged in as "instructor2" with "sakai"
	And I'm in the "External Tool" tool of "LTI AFT Test 1" site
	When I click the "Edit" link
    When I supply "" to "#imsti\.launch"
    And I supply "" to "#imsti\.key"
    And I supply "" to "#imsti\.secret"
    And I click "Update Options"
    Then I get the error message "You must set Launch URL, key, and secret"


@skip
@logout
Scenario: 2.0.11 Configuration Screen
    Given I am logged in as "instructor2" with "sakai"
	And I'm in the "External Tool" tool of "LTI AFT Test 1" site
	And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I click "Update Options"
    Then I get the page with "Launch Validated."
    And The user is an Instructor

@skip
@logout
Scenario: 2.0.12 - Verify student role
    Given I am logged in as "student0001" with "sakai"
	And I'm in the "External Tool" tool of "LTI AFT Test 1" site
	Then I get the page with "Launch Validated."
	And The user is a Learner

@skip
@logout
Scenario: 2.0.13 - Configuration Screen - Clear values
    Given I am logged in as "instructor2" with "sakai"
	And I'm in the "External Tool" tool of "LTI AFT Test 1" site
	And I click the "Edit" link
    And I click the "Clear Stored Preferences" link
    And I click "YES"
    Then I click the "External Tool" link
    And The text "This tool has not yet been configured." displays

@skip
@logout
Scenario: 2.0.14 - Student views unconfigured tool
    Given I am logged in as "student0001" with "sakai"
	And I'm in the "External Tool" tool of "LTI AFT Test 1" site
	Then The text "This tool has not yet been configured." displays

@skip
@logout
Scenario: 2.0.15 Configuration Screen
    Given I am logged in as "instructor2" with "sakai"
	And I'm in the "External Tool" tool of "LTI AFT Test 1" site
	And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "xxxx" to "#imsti\.secret"
    And I click "Update Options"
    Then I get an invalid launch


#This one is currently broken (as of 11/29).  The tool title and launch button values are switched
@skip
@logout
Scenario: 2.0.16 and .17 Configuration Screen - button, tool text, new window
    Given I am logged in as "instructor2" with "sakai"
	And I'm in the "External Tool" tool of "LTI AFT Test 1" site
	And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I supply "foobar" to "#imsti\.pagetitle"
    And I supply "doodah" to "#imsti\.tooltitle"
    And I "check" the box to open a new window
    And I click "Update Options"
    Then The Tool title changes to "foobar"
    And The Launch button label is "Launch doodah"
    Then I get the page with "Launch Validated." opened in a new window

@skip
@logout
Scenario: 2.0.16 and .17 Configuration Screen - reset the tool title and stuff so following tests work
    Given I am logged in as "instructor2" with "sakai"
	And I'm in the "foobar" tool of "LTI AFT Test 1" site
    And I get the page with "Launch Validated." opened in a new window
	And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I supply "" to "#imsti\.pagetitle"
    And I supply "" to "#imsti\.tooltitle"
    And I "uncheck" the box to open a new window
    And I click "Update Options"
    Then The Tool title changes to "External Tool"
    And I get the page with "Launch Validated."

@skip
@logout
Scenario: 2.0.19 Configuration Screen - frame height
    Given I am logged in as "instructor2" with "sakai"
    And I'm in the "External Tool" tool of "LTI AFT Test 1" site
    And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I supply "" to "#imsti\.pagetitle"
    And I supply "" to "#imsti\.tooltitle"
    And I "uncheck" the box to open a new window
    And I supply "777" to "#imsti\.frameheight"
    And I click "Update Options"
    Then I get the page with "Launch Validated."
    And The iframe height is "777"

@skip
@logout
Scenario: 2.0.20 Configuration Screen - debug launch
    Given I am logged in as "instructor2" with "sakai"
    And I'm in the "External Tool" tool of "LTI AFT Test 1" site
    And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I supply "" to "#imsti\.pagetitle"
    And I supply "" to "#imsti\.tooltitle"
    And I "uncheck" the box to open a new window
    And I supply "" to "#imsti\.frameheight"
    And I "check" the box to debug
    And I click "Update Options"
    Then I should see a button with label "Press to continue to external tool."
    And I should see a button with label "Show Launch Data"
    When I click "Show Launch Data" in the iframe
    Then I should see key/value pairs
    When I click "Press to continue to external tool." in the iframe
    Then I get the page with "Launch Validated."

@skip
@logout
Scenario Outline: 2.0.21 and .22 Configuration Screen - do/don't send name/email
    Given I am logged in as "instructor2" with "sakai"
    And I'm in the "External Tool" tool of "LTI AFT Test 1" site
    And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I supply "" to "#imsti\.pagetitle"
    And I supply "" to "#imsti\.tooltitle"
    And I "uncheck" the box to open a new window
    And I supply "" to "#imsti\.frameheight"
    And I "uncheck" the box to debug
    And I "<check_action>" the box to send names
    And I "<check_action>" the box to send email
    And I click "Update Options"
    Then I get the page with "Launch Validated."
    And I "<view_result>" see name/email data
    Examples:
        |check_action   |view_result    |
        |check          |should         |
        |uncheck        |should not     |

@skip
@logout
Scenario Outline: 2.0.23 and .24 Configuration Screen - custom params
    Given I am logged in as "instructor2" with "sakai"
    And I'm in the "External Tool" tool of "LTI AFT Test 1" site
    And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I supply "" to "#imsti\.pagetitle"
    And I supply "" to "#imsti\.tooltitle"
    And I "uncheck" the box to open a new window
    And I supply "" to "#imsti\.frameheight"
    And I "uncheck" the box to debug
    And I supply "<data_in>" to "#imsti\.custom"
    And I click "Update Options"
    Then I get the page with "Launch Validated."
    And I should see "<data_out1>" in custom data
    And I should see "<data_out2>" in custom data
    Examples:
        |data_in                |data_out1                  |data_out2                  |
        |x=123;y=456            |custom_x=123 (ASCII)\n     |custom_y=456 (ASCII)\n     |
        |a.b+c=789\nl_mN=543    |custom_a_b_c=789 (ASCII)\n |custom_l_mn=543 (ASCII)\n  |

@skip
@logout
Scenario: 2.0.25 Configuration Screen - cancel
    Given I am logged in as "instructor2" with "sakai"
    And I'm in the "External Tool" tool of "LTI AFT Test 1" site
    And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I supply "" to "#imsti\.pagetitle"
    And I supply "" to "#imsti\.tooltitle"
    And I "uncheck" the box to open a new window
    And I supply "" to "#imsti\.frameheight"
    And I "uncheck" the box to debug
    And I supply "foo=bar" to "#imsti\.custom"
    And I click "Update Options"
    Then I get the page with "Launch Validated."
    And I "should" see "foo=bar" in custom data
    When I click the "Edit" link
    And I supply "doo=dah" to "#imsti\.custom"
    And I click "Cancel"
    Then I get the page with "Launch Validated."
    And I "should not" see "doo=dah" in custom data

@skip
@logout
Scenario: 2.0.26 Configuration Screen - clear, then no
    Given I am logged in as "instructor2" with "sakai"
    And I'm in the "External Tool" tool of "LTI AFT Test 1" site
    And I click the "Edit" link
    When I supply "https://online.dr-chuck.com/sakai-api-test/tool.php" to "#imsti\.launch"
    And I supply "12345" to "#imsti\.key"
    And I supply "secret" to "#imsti\.secret"
    And I supply "" to "#imsti\.pagetitle"
    And I supply "" to "#imsti\.tooltitle"
    And I "uncheck" the box to open a new window
    And I supply "" to "#imsti\.frameheight"
    And I "uncheck" the box to debug
    And I supply "foo=bar" to "#imsti\.custom"
    And I click "Update Options"
    Then I get the page with "Launch Validated."
    When I click the "Edit" link
    And I click the "Clear Stored Preferences" link
    And I click "NO"
    Then The "#imsti\.launch" field has the value "https://online.dr-chuck.com/sakai-api-test/tool.php"

#@skip
@logout
Scenario Outline: 99 Site Cleanup
    Given I am logged in as "admin" with "admin"
    When I delete the site "<site_name>"
    Then Site deleted with name "<site_name>"
    Examples:
        |site_name      |
        |LTI AFT Test 1 |