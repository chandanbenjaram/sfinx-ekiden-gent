/*
 * Copyright 2011 Peter Kuterna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.sfinxekidengent.receiver;

import be.sfinxekidengent.service.CfpSyncManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

/**
 * {@link BroadcastReceiver} that is called when the boot sequence as ended. It
 * installs the alarm to trigger background updates.
 */
public class OnBootReceiver extends BroadcastReceiver {

	private static final long FIRST_TRIGGER = 60 * DateUtils.SECOND_IN_MILLIS;

	@Override
	public void onReceive(Context context, Intent intent) {
		CfpSyncManager manager = new CfpSyncManager(context);
		manager.setSyncAlarm(FIRST_TRIGGER);
	}

}
