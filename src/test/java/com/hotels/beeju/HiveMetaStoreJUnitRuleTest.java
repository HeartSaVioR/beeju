/**
 * Copyright (C) 2015-2019 Expedia, Inc.
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
package com.hotels.beeju;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.InvalidObjectException;
import org.apache.thrift.TException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HiveMetaStoreJUnitRuleTest {

  public @Rule HiveMetaStoreJUnitRule hiveDefaultName = new HiveMetaStoreJUnitRule();

  public @Rule HiveMetaStoreJUnitRule hiveCustomName = new HiveMetaStoreJUnitRule("my_test_database");

  private static File tempRoot;

  @Before
  public void before() {
    tempRoot = hiveDefaultName.temporaryFolder.getRoot();
    assertTrue(tempRoot.exists());
  }

  @Test
  public void hiveDefaultName() throws Exception {
    assertRuleInitialised(hiveDefaultName);
  }

  @Test
  public void hiveCustomName() throws Exception {
    assertRuleInitialised(hiveCustomName);
  }

  private static void assertRuleInitialised(HiveMetaStoreJUnitRule hive) throws Exception {
    String databaseName = hive.databaseName();

    Database database = hive.client().getDatabase(databaseName);

    assertThat(database.getName(), is(databaseName));
    File databaseFolder = new File(hive.temporaryFolder.getRoot(), databaseName);
    assertThat(new File(database.getLocationUri()) + "/", is(databaseFolder.toURI().toString()));
  }

  @Test
  public void customProperties() {
    Map<String, String> conf = new HashMap<>();
    conf.put("my.custom.key", "my.custom.value");
    HiveConf hiveConf = new HiveMetaStoreJUnitRule("db", conf).conf();
    assertThat(hiveConf.get("my.custom.key"), is("my.custom.value"));
  }

  @Test(expected = AlreadyExistsException.class)
  public void createExistingDatabase() throws TException {
    hiveDefaultName.createDatabase(hiveDefaultName.databaseName());
  }

  @Test(expected = NullPointerException.class)
  public void createDatabaseNullName() throws TException {
    hiveDefaultName.createDatabase(null);
  }

  @Test(expected = InvalidObjectException.class)
  public void createDatabaseInvalidName() throws TException {
    hiveDefaultName.createDatabase("");
  }

  @AfterClass
  public static void afterClass() {
    assertFalse(tempRoot.exists());
  }

}
