/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.util.ResourceLoader;

/* For publish/republish notification backing bean. */
@Slf4j
@ManagedBean(name="publishRepublishNotification")
@SessionScoped
@Data
public class PublishRepublishNotificationBean implements Serializable {

	private static final ResourceLoader res = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");

	private String notificationSubject;
	private String siteTitle;
	private boolean sendNotification;

	public ArrayList<SelectItem> getNotificationLevelChoices() {
		ArrayList<SelectItem> list = new ArrayList<SelectItem>();
		list.add(new SelectItem("1", res.getString("no_notification")));
		list.add(new SelectItem("2", res.getString("send_notification")));
		return list;
	}

}
