/**
 * Copyright (C) 2011 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.connid.bundles.ad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.connid.bundles.ad.util.ADUtilities;
import org.connid.bundles.ldap.LdapConfiguration;
import org.connid.bundles.ldap.commons.LdapConstants;
import org.connid.bundles.ldap.commons.ObjectClassMappingConfig;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public class ADConfiguration extends LdapConfiguration {

    private final Log LOG = Log.getLog(ADConfiguration.class);

    private boolean ssl = true;

    private boolean retrieveDeletedUser = true;

    private boolean retrieveDeletedGroup = true;

    public static final String PROMPT_USER_FLAG = "pwdLastSet";

    public static final String PROMPT_USER_VALUE = "0";

    public static final String LOCK_OUT_FLAG = "lockoutTime";

    public static final String LOCK_OUT_DEFAULT_VALUE = "0";

    public static final String UCCP_FLAG = "userCannotChangePassword";

    public static final String CN_NAME = "CN";

    private List<String> memberships;

    private boolean trustAllCerts;

    private boolean membershipsInOr = false;

    private boolean startSyncFromToday = true;

    private String defaultPeopleContainer;

    private String defaultGroupContainer;

    private SearchScope userSearchScope;

    private SearchScope groupSearchScope;

    private String groupSearchFilter;

    private String[] groupBaseContexts = {};

    private String[] userBaseContexts = {};

    private String groupMemberReferenceAttribute = "member";

    private String groupOwnerReferenceAttribute = "managedBy";

    private boolean pwdUpdateOnly = false;

    private final ObjectClassMappingConfig accountConfig = new ObjectClassMappingConfig(
            ObjectClass.ACCOUNT,
            CollectionUtil.newList("top", "person", "organizationalPerson", "user"),
            false,
            CollectionUtil.newList("sAMAccountName", "cn", "member"),
            LdapConstants.PASSWORD);

    private final ObjectClassMappingConfig groupConfig = new ObjectClassMappingConfig(
            ObjectClass.GROUP,
            CollectionUtil.newList("top", "group"),
            false,
            Collections.<String>emptyList());

    public ADConfiguration() {
        super();

        setUidAttribute("sAMAccountName");
        setSynchronizePasswords(false);
        setAccountUserNameAttributes("sAMAccountName");
        setObjectClassesToSynchronize(new String[] { "user" });
        setGroupMemberAttribute("member");

        setUsePagedResultControl(true);
        setBlockSize(100);
        setUseBlocks(true);

        setPasswordAttribute("unicodePwd");
        setPort(636);

        memberships = new ArrayList<String>();

        userSearchScope = SearchScope.subtree;
        groupSearchScope = SearchScope.subtree;
    }

    @Override
    @ConfigurationProperty(displayMessageKey = "ssl.display",
            helpMessageKey = "ssl.help", order = 1)
    public boolean isSsl() {
        return ssl;
    }

    @Override
    public void setSsl(final boolean ssl) {
        super.setSsl(ssl);
        this.ssl = ssl;
    }

    @Override
    public Map<ObjectClass, ObjectClassMappingConfig> getObjectClassMappingConfigs() {
        HashMap<ObjectClass, ObjectClassMappingConfig> result = new HashMap<ObjectClass, ObjectClassMappingConfig>();
        result.put(accountConfig.getObjectClass(), accountConfig);
        result.put(groupConfig.getObjectClass(), groupConfig);
        return result;
    }

    @ConfigurationProperty(displayMessageKey = "memberships.display",
            helpMessageKey = "memberships.help", order = 1)
    public String[] getMemberships() {
        return memberships.toArray(new String[memberships.size()]);
    }

    public void setMemberships(final String... memberships) {
        this.memberships = new ArrayList<String>();

        if (memberships != null) {
            for (String membership : memberships) {
                if (ADUtilities.isDN(membership)) {
                    this.memberships.add(membership.trim());
                } else {
                    LOG.warn("Skip membership! \"{0}\" is not a valid distinguished name (DN)", membership);
                }
            }
        }
    }

    @ConfigurationProperty(displayMessageKey = "retrieveDeletedUser.display",
            helpMessageKey = "retrieveDeletedUser.help", order = 2)
    public boolean isRetrieveDeletedUser() {
        return retrieveDeletedUser;
    }

    public void setRetrieveDeletedUser(boolean retrieveDeletedUser) {
        this.retrieveDeletedUser = retrieveDeletedUser;
    }

    @ConfigurationProperty(displayMessageKey = "retrieveDeletedGroup.display",
            helpMessageKey = "retrieveDeletedGroup.help", order = 3)
    public boolean isRetrieveDeletedGroup() {
        return this.retrieveDeletedGroup;
    }

    public void setRetrieveDeletedGroup(boolean retrieveDeletedGroup) {
        this.retrieveDeletedGroup = retrieveDeletedGroup;
    }

    @ConfigurationProperty(displayMessageKey = "trustAllCerts.display",
            helpMessageKey = "trustAllCerts.help", order = 4)
    public boolean isTrustAllCerts() {
        return trustAllCerts;
    }

    public void setTrustAllCerts(final boolean trustAllCerts) {
        this.trustAllCerts = trustAllCerts;
    }

    public boolean isMembershipsInOr() {
        return membershipsInOr;
    }

    @ConfigurationProperty(displayMessageKey = "membershipsInOr.display",
            helpMessageKey = "membershipsInOr.help", order = 5)
    public void setMembershipsInOr(boolean membershipsInOr) {
        this.membershipsInOr = membershipsInOr;
    }

    @ConfigurationProperty(order = 6, required = true,
            displayMessageKey = "baseContextsToSynchronize.display",
            helpMessageKey = "baseContextsToSynchronize.help")
    @Override
    public String[] getBaseContextsToSynchronize() {
        return super.getBaseContextsToSynchronize();
    }

    @Override
    public void setBaseContextsToSynchronize(String... baseContextsToSynchronize) {
        super.setBaseContextsToSynchronize(baseContextsToSynchronize);
    }

    @ConfigurationProperty(displayMessageKey = "defaultPeopleContainer.display",
            helpMessageKey = "defaultPeopleContainer.help", order = 7)
    public String getDefaultPeopleContainer() {
        if (StringUtil.isBlank(defaultPeopleContainer)) {
            return getBaseContextsToSynchronize() == null || getBaseContextsToSynchronize().length < 1
                    ? null : getBaseContextsToSynchronize()[0];
        } else {
            return defaultPeopleContainer;
        }
    }

    public void setDefaultPeopleContainer(String defaultPeopleContainer) {
        this.defaultPeopleContainer = defaultPeopleContainer;
    }

    @ConfigurationProperty(displayMessageKey = "defaultGroupContainer.display",
            helpMessageKey = "defaultGroupContainer.help", order = 8)
    public String getDefaultGroupContainer() {
        if (StringUtil.isBlank(defaultGroupContainer)) {
            return getBaseContextsToSynchronize() == null || getBaseContextsToSynchronize().length < 1
                    ? null : getBaseContextsToSynchronize()[0];
        } else {
            return defaultGroupContainer;
        }
    }

    public void setDefaultGroupContainer(String defaultGroupContainer) {
        this.defaultGroupContainer = defaultGroupContainer;
    }

    @ConfigurationProperty(displayMessageKey = "userSearchScope.display",
            helpMessageKey = "userSearchScope.help", order = 9)
    public String getUserSearchScope() {
        return userSearchScope == null ? SearchScope.subtree.toString() : userSearchScope.toString();
    }

    public void setUserSearchScope(final String userSearchScope) {
        this.userSearchScope = SearchScope.valueOf(userSearchScope.toLowerCase());
    }

    @ConfigurationProperty(displayMessageKey = "groupSearchScope.display",
            helpMessageKey = "groupSearchScope.help", order = 10)
    public String getGroupSearchScope() {
        return groupSearchFilter == null ? SearchScope.subtree.toString() : groupSearchScope.toString();
    }

    public void setGroupSearchScope(final String groupSearchScope) {
        this.groupSearchScope = SearchScope.valueOf(groupSearchScope.toLowerCase());
    }

    @ConfigurationProperty(displayMessageKey = "groupSearchFilter.display",
            helpMessageKey = "groupSearchFilter.help", order = 11)
    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    @ConfigurationProperty(displayMessageKey = "groupBaseContexts.display",
            helpMessageKey = "groupBaseContexts.help", order = 12)
    public String[] getGroupBaseContexts() {
        if (groupBaseContexts != null && groupBaseContexts.length > 0) {
            // return specified configuration
            return groupBaseContexts.clone();
        } else {
            // return root suffixes
            return getBaseContextsToSynchronize();
        }
    }

    public void setGroupBaseContexts(String... baseContexts) {
        this.groupBaseContexts = baseContexts.clone();
        // update base context everytime ...
        super.setBaseContexts(this.getBaseContexts());
    }

    @ConfigurationProperty(displayMessageKey = "userBaseContexts.display",
            helpMessageKey = "userBaseContexts.help", order = 13)
    public String[] getUserBaseContexts() {
        if (userBaseContexts != null && userBaseContexts.length > 0) {
            // return specified configuration
            return userBaseContexts.clone();
        } else {
            // return root suffixes
            return getBaseContextsToSynchronize();
        }
    }

    public void setUserBaseContexts(final String... baseContexts) {
        this.userBaseContexts = baseContexts.clone();
        // update base context everytime ...
        super.setBaseContexts(this.getBaseContexts());
    }

    @Override
    public String[] getBaseContexts() {
        final List<String> res = new ArrayList<String>();
        res.addAll(Arrays.asList(getUserBaseContexts()));
        res.addAll(Arrays.asList(getGroupBaseContexts()));
        return res.toArray(new String[res.size()]);
    }

    @ConfigurationProperty(displayMessageKey = "groupMemberReferenceAttribute.display",
            helpMessageKey = "groupMemberReferenceAttribute.help", order = 14)
    public String getGroupMemberReferenceAttribute() {
        return StringUtil.isBlank(groupMemberReferenceAttribute) ? "member" : groupMemberReferenceAttribute;
    }

    public void setGroupMemberReferenceAttribute(String groupMemberReferenceAttribute) {
        this.groupMemberReferenceAttribute = groupMemberReferenceAttribute;
    }

    @ConfigurationProperty(displayMessageKey = "groupOwnerReferenceAttribute.display",
            helpMessageKey = "groupOwnerReferenceAttribute.help", order = 15)
    public String getGroupOwnerReferenceAttribute() {
        return StringUtil.isBlank(groupOwnerReferenceAttribute) ? "managedBy" : groupOwnerReferenceAttribute;
    }

    public void setGroupOwnerReferenceAttribute(String groupOwnerReferenceAttribute) {
        this.groupOwnerReferenceAttribute = groupOwnerReferenceAttribute;
    }

    @ConfigurationProperty(displayMessageKey = "startSyncFromToday.display",
            helpMessageKey = "startSyncFromToday.help", order = 16)
    public boolean isStartSyncFromToday() {
        return startSyncFromToday;
    }

    public void setStartSyncFromToday(boolean startSyncFromToday) {
        this.startSyncFromToday = startSyncFromToday;
    }

    public boolean isPwdUpdateOnly() {
        return pwdUpdateOnly;
    }

    @ConfigurationProperty(displayMessageKey = "pwdUpdateOnly.display",
            helpMessageKey = "pwdUpdateOnly.help", required = true, order = 17)
    public void setPwdUpdateOnly(boolean pwdUpdateOnly) {
        this.pwdUpdateOnly = pwdUpdateOnly;
    }

    @Override
    public final void setUidAttribute(final String uidAttribute) {
        setAccountUserNameAttributes("sAMAccountName", uidAttribute);
        super.setUidAttribute(uidAttribute);
    }

    public enum SearchScope {

        object,
        onelevel,
        subtree

    }
}
