package org.sakaiproject.acceptancetests.stepdefs.lti;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sakaiproject.acceptancetests.collaborator.WebDriverManager;

import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;

public class BasicLti {

    WebDriver driver;
    String windowHandle;

    @After("@logout")
    public void doLogout() {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("a#loginUser, #loginUser > a"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
    }

    @Given("^I am using \"([^\"]*)\"$")
    public void i_am_using(final String url) throws Exception {
        this.driver = WebDriverManager.getDriver();
        this.driver.navigate().to(url);
        this.windowHandle = driver.getWindowHandle();
    }

    @Given("^I am logged in as \"([^\"]*)\" with \"([^\"]*)\"$")
    public void i_am_logged_in_as(String username, String password) {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#eid"))).sendKeys(username);
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#pw"))).sendKeys(password);
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#submit"))).click();
    }

    @When("^I create a new site \"([^\"]*)\"$")
    public void i_create_a_new_site(String siteName) throws Exception {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText("Worksite Setup"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText("Create New Site"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input#project"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input#submitBuildOwn"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#title"))).sendKeys(siteName);
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name=continue][type=submit]"))).click();

        //Need to escape the . in the id since selectors hate that!
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input#sakai\\.basiclti"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input#btnContinue"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name=Continue][type=submit]"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input#continueButton"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input#addSite"))).click();

    }

    @Then("Site created with name \"([^\"]*)\"$")
    public void site_created_with_name(String siteName) {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#flashNotif")));
        final WebElement link = this.driver.findElement(By.linkText(siteName));
        Assert.assertEquals(siteName, StringUtils.trim(link.getText()));
    }

    private void findSite(String siteName) {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText("Worksite Setup"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#searchFilter1"))).sendKeys(siteName);
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#btnSearch_searchFilter1"))).click();

        //Make sure there is only one
        final WebElement div = this.driver.findElement(By.cssSelector(".sakai-table-pagerLabel"));
        Assert.assertEquals("Viewing 1 - 1 of 1 items", StringUtils.trim(div.getText()));

        //Click the check box
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#site1"))).click();
    }

    @When("I delete the site \"([^\"]*)\"$")
    public void i_delete_the_site(String siteName) {
        findSite(siteName);

        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#btnHardDelete1"))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name=eventSubmit_doSite_delete_confirmed]"))).click();
    }

    @Then("Site deleted with name \"([^\"]*)\"$")
    public void site_deleted_with_name(String siteName) {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.sakai-table-noResultsMessage")));
        final WebElement link = this.driver.findElement(By.cssSelector("div.sakai-table-noResultsMessage"));
        Assert.assertEquals("No sites were found that match your search for \"" + siteName + "\" in the view of \"All My Sites\".", StringUtils.trim(link.getText()));
    }

    @Then("^User \"([^\"]*)\" added to site \"([^\"]*)\"  as role \"([^\"]*)\"$")
    public void user_added_to_site_as_role(String username, String siteName, String roleName) {
        findSite(siteName);
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#btnEdit1"))).click();

        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText("Add Participants"))).click();

        new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#content\\:\\:officialAccountParticipant"))).sendKeys(username);
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value=Continue]"))).click();

        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value=" + roleName + "]"))).click();

        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value=Continue]"))).click();

        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value=Continue]"))).click();

        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value=Finish]"))).click();
    }

    @When("^I click the \"([^\"]*)\" link$")
    public void i_click_the_link(String linkText) throws Exception {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText(linkText))).click();
    }

    @Given("^I'm in the \"([^\"]*)\" tool of \"([^\"]*)\" site$")
    public void i_m_in_the_tool_of_site_as_user(String toolName, String siteName) {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText(siteName))).click();
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText(toolName))).click();
    }

    @When("^I click \"([^\"]*)\"$")
    public void i_click(String buttonName) throws Exception {
        new WebDriverWait(this.driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='" + buttonName + "']"))).click();
    }

    @When("^I click \"([^\"]*)\" in the iframe$")
    public void i_click_in_the_iframe(String buttonName) throws Exception {
        switchToFrame();
        i_click(buttonName);
        backToParentFrame();
    }

    @When("^I supply \"([^\"]*)\" to \"([^\"]*)\"$")
    public void i_supply_value_to_field(String value, String field) throws Exception {
        final WebElement launchElement = new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(field)));
        launchElement.clear();
        launchElement.sendKeys(value);
    }

    @Then("I get the error message \"([^\"]*)\"$")
    public void i_get_the_error_message(String errorMessage) {
        final WebElement div = this.driver.findElement(By.cssSelector("div.alertMessage"));
        Assert.assertEquals(errorMessage, StringUtils.trim(div.getText()));
    }

    @Then("I \"([^\"]*)\" see an \"([^\"]*)\" button$")
    public void i_see_an_button(String conditionalText, String buttonName) {
        final List<WebElement> buttons = this.driver.findElements(By.cssSelector("a#jsr-edit"));
        if ("should not".equalsIgnoreCase(conditionalText)) {
            Assert.assertTrue(buttons.isEmpty());
        } else if ("should".equalsIgnoreCase(conditionalText)) {
            Assert.assertFalse(buttons.isEmpty());
            Assert.assertEquals(buttonName, StringUtils.trim(buttons.get(0).getText()));
        }
    }

    @Then("I should see a button with label \"([^\"]*)\"$")
    public void i_should_see_a_button(String buttonName) {
        switchToFrame();

        final WebElement button = this.driver.findElement(By.cssSelector("input[value='" + buttonName + "']"));
        Assert.assertNotNull(button);
        Assert.assertEquals(buttonName, StringUtils.trim(button.getAttribute("value")));

        backToParentFrame();
    }

    @Then("^The text \"([^\"]*)\" displays$")
    public void the_text_displays(String messageText) {
        final WebElement div = this.driver.findElement(By.cssSelector("div.Mrphs-toolBody--sakai-basiclti"));
        Assert.assertEquals(messageText, StringUtils.trim(div.getText()));
    }

    @Then("^I get the page with \"([^\"]*)\"$")
    public void i_get_the_page_with(String messageText) {
        switchToFrame();

        final WebElement p = this.driver.findElement(By.cssSelector("p[style='color:green']"));
        Assert.assertEquals(messageText, StringUtils.trim(p.getText()));

        backToParentFrame();
    }

    @Then("^I get the page with \"([^\"]*)\" opened in a new window$")
    public void  i_get_the_page_with_in_a_new_window(String messageText) {
        //Get the handle to the new window
        Set<String> windowHandles = driver.getWindowHandles();
        windowHandles.remove(this.windowHandle);
        String[] handles = windowHandles.toArray(new String[0]);

        //Switch to the window
        this.driver.switchTo().window(handles[0]);

        final WebElement p = this.driver.findElement(By.cssSelector("p[style='color:green']"));
        Assert.assertEquals(messageText, StringUtils.trim(p.getText()));

        //Close the new window
        this.driver.close();

        //Switch back to the main window
        this.driver.switchTo().window(windowHandle);
    }

    @Then("^The user is an Instructor$")
    public void the_user_is_an_instructor() {
        doStuffInIframe("isInstructor() = true", "roles=Instructor", true);
    }

    @Then("^The user is a Learner$")
    public void the_user_is_a_learner() {
        doStuffInIframe("isInstructor() = false", "roles=Learner", true);
    }

    private void switchToFrame() {
        final WebElement iframe = this.driver.findElement(By.cssSelector("iframe"));
        //Switch to the iframe
        this.driver.switchTo().frame(iframe);
    }

    private void backToParentFrame() {
        //Switch back to the parent
        this.driver.switchTo().parentFrame();
    }

    private void doStuffInIframe(String pre1Text, String pre2Text, boolean contains) {
        switchToFrame();

        final List<WebElement> pres = this.driver.findElements(By.cssSelector("pre"));
        if (pre1Text != null) {
            Assert.assertThat(contains, is(pres.get(0).getText().contains(pre1Text)));
        }

        if (pre2Text != null) {
            Assert.assertThat(contains, is(pres.get(1).getText().contains(pre2Text)));
        }

        backToParentFrame();
    }

    @Then("^I get an invalid launch$")
    public void i_get_an_invalid_launch() {
        switchToFrame();

        final WebElement p = this.driver.findElement(By.cssSelector("p[style='color:red']"));
        Assert.assertTrue(p.getText().contains("Could not establish context: Invalid signature"));

        backToParentFrame();
    }

    private void check_uncheck_checkbox(String checkOption, WebElement checkbox) {
        if ("check".equalsIgnoreCase(checkOption)) {
            if(!checkbox.isSelected()) {
                checkbox.click();
            }
        } else if ("uncheck".equalsIgnoreCase(checkOption)) {
            if(checkbox.isSelected()) {
                checkbox.click();
            }
        }
    }

    @When("^I \"([^\"]*)\" the box to open a new window$")
    public void i_check_the_box_to_open_a_new_window(String checkOption) {
        final WebElement checkbox = this.driver.findElement(By.cssSelector("input#imsti\\.newpage"));
        check_uncheck_checkbox(checkOption, checkbox);
    }

    @When("^I \"([^\"]*)\" the box to send names$")
    public void i_check_the_box_to_send_names(String checkOption) {
        final WebElement checkbox = this.driver.findElement(By.cssSelector("input#imsti\\.releasename"));
        check_uncheck_checkbox(checkOption, checkbox);
    }

    @When("^I \"([^\"]*)\" the box to send email$")
    public void i_check_the_box_to_send_email_to_external_tool(String checkOption) {
        final WebElement checkbox = this.driver.findElement(By.cssSelector("input#imsti\\.releaseemail"));
        check_uncheck_checkbox(checkOption, checkbox);
    }

    @When("^I \"([^\"]*)\" the box to debug$")
    public void i_check_the_box_to_debug(String checkOption) {
        final WebElement checkbox = this.driver.findElement(By.cssSelector("input#imsti\\.debug"));
        check_uncheck_checkbox(checkOption, checkbox);
    }

    @Then("^The Tool title changes to \"([^\"]*)\"$")
    public void the_tool_title_changes_to(String toolTitle) {
        final WebElement span = this.driver.findElement(By.cssSelector("a.Mrphs-hierarchy--toolName > span.Mrphs-breadcrumb--toolNameText"));
        Assert.assertEquals(toolTitle.toLowerCase(), span.getText().toLowerCase());

    }

    @Then("^The Launch button label is \"([^\"]*)\"$")
    public void the_launch_button_label_is(String buttonLabel) {
        final WebElement button = this.driver.findElement(By.cssSelector("input[target='BasicLTI']"));
        Assert.assertEquals(buttonLabel.toLowerCase(), button.getAttribute("value").toLowerCase());

    }

    @Then("^The iframe height is \"([^\"]*)\"$")
    public void the_iframe_height_is(String frameHeight) {
        final WebElement iframe = this.driver.findElement(By.cssSelector("iframe"));
        Assert.assertEquals(frameHeight, iframe.getAttribute("height"));
    }

    @Then("^I should see key/value pairs$")
    public void i_should_see_key_value_pairs() {
        switchToFrame();

        final WebElement pre = this.driver.findElement(By.cssSelector("pre[id^=ltiLaunchDebug_]"));
        Assert.assertTrue(pre.getText().contains("context_title=LTI AFT Test 1"));
        Assert.assertTrue(pre.getText().contains("oauth_consumer_key=12345"));

        backToParentFrame();
    }

    @Then("^I \"([^\"]*)\" see name/email data$")
    public void i_should_see_name_email_data(String conditionalText) {
        boolean contains = false;
        if ("should".equalsIgnoreCase(conditionalText)) {
            contains = true;
        }

        doStuffInIframe(null, "lis_person_contact_email_primary=instructor2@example.edu", contains);
        doStuffInIframe(null, "lis_person_name_family=Instructor2", contains);
        doStuffInIframe(null, "lis_person_name_full=Sakai Instructor2", contains);
        doStuffInIframe(null, "lis_person_name_given=Sakai", contains);
    }

    @Then("^I \"([^\"]*)\" see \"([^\"]*)\" in custom data$")
    public void i_should_see_custom_data(String conditionalText, String data) {
        boolean contains = false;
        if ("should".equalsIgnoreCase(conditionalText)) {
            contains = true;
        }

        doStuffInIframe(null, data, contains);
    }

    @Then("^The \"([^\"]*)\" field has the value \"([^\"]*)\"$")
    public void the_field_has_the_value(String field, String value) {
        final WebElement launchElement = new WebDriverWait(this.driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(field)));
        Assert.assertEquals(value, launchElement.getAttribute("value"));
    }

}
