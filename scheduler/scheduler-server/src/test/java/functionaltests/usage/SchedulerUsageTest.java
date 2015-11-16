/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2014 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.usage;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.usage.JobUsage;
import org.junit.Test;

import functionaltests.SchedulerConsecutive;
import functionaltests.SchedulerTHelper;

import static org.junit.Assert.*;


public class SchedulerUsageTest extends SchedulerConsecutive {

    @Test
    public void action() throws Exception {

        SchedulerAuthenticationInterface auth = SchedulerTHelper.getSchedulerAuth();
        PublicKey pubKey = auth.getPublicKey();

        Credentials cred = Credentials.createCredentials(new CredData(SchedulerTHelper.user_username,
          SchedulerTHelper.user_password), pubKey);

        Scheduler scheduler = auth.login(cred);

        fallbackToMyAccountUsage(scheduler);
    }

    private void fallbackToMyAccountUsage(Scheduler scheduler) throws NotConnectedException, PermissionException {
        List<JobUsage> asAUser = scheduler.getMyAccountUsage(new Date(0), new Date(0));
        assertNotNull(asAUser);

        // fallback to my account usage
        List<JobUsage> forMyUser = scheduler.getAccountUsage(SchedulerTHelper.user_username, new Date(0), new Date(0));
        assertNotNull(forMyUser);

        try {
            scheduler.getAccountUsage(SchedulerTHelper.admin_username, new Date(0), new Date(0));
            fail("Should throw permission exception");
        } catch (PermissionException e) {
        }
    }
}
