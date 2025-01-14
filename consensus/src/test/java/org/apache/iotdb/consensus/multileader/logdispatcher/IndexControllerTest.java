/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.consensus.multileader.logdispatcher;

import org.apache.ratis.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class IndexControllerTest {

  private static final File storageDir = new File("target" + java.io.File.separator + "test");
  private static final String prefix = "version";

  @Before
  public void setUp() throws IOException {
    FileUtils.createDirectories(storageDir);
  }

  @After
  public void tearDown() throws IOException {
    FileUtils.deleteFully(storageDir);
  }

  /** test indexController when incrementIntervalAfterRestart == true */
  @Test
  public void testTrueIncrementIntervalAfterRestart() {
    IndexController controller = new IndexController(storageDir.getAbsolutePath(), prefix, true);
    Assert.assertEquals(0, controller.getCurrentIndex());
    Assert.assertEquals(0, controller.getLastFlushedIndex());

    for (int i = 0; i < IndexController.FLUSH_INTERVAL - 1; i++) {
      controller.incrementAndGet();
    }
    Assert.assertEquals(IndexController.FLUSH_INTERVAL - 1, controller.getCurrentIndex());
    Assert.assertEquals(0, controller.getLastFlushedIndex());

    controller = new IndexController(storageDir.getAbsolutePath(), prefix, true);
    Assert.assertEquals(IndexController.FLUSH_INTERVAL, controller.getCurrentIndex());
    Assert.assertEquals(IndexController.FLUSH_INTERVAL, controller.getLastFlushedIndex());

    for (int i = 0; i < IndexController.FLUSH_INTERVAL + 1; i++) {
      controller.incrementAndGet();
    }
    Assert.assertEquals(IndexController.FLUSH_INTERVAL * 2 + 1, controller.getCurrentIndex());
    Assert.assertEquals(IndexController.FLUSH_INTERVAL * 2, controller.getLastFlushedIndex());

    controller = new IndexController(storageDir.getAbsolutePath(), prefix, true);
    Assert.assertEquals(IndexController.FLUSH_INTERVAL * 3, controller.getCurrentIndex());
    Assert.assertEquals(IndexController.FLUSH_INTERVAL * 3, controller.getLastFlushedIndex());
  }

  /** test indexController when incrementIntervalAfterRestart == false */
  @Test
  public void testFalseIncrementIntervalAfterRestart() {
    IndexController controller = new IndexController(storageDir.getAbsolutePath(), prefix, false);
    Assert.assertEquals(0, controller.getCurrentIndex());
    Assert.assertEquals(0, controller.getLastFlushedIndex());

    controller.updateAndGet(IndexController.FLUSH_INTERVAL - 1);

    Assert.assertEquals(IndexController.FLUSH_INTERVAL - 1, controller.getCurrentIndex());
    Assert.assertEquals(0, controller.getLastFlushedIndex());

    controller = new IndexController(storageDir.getAbsolutePath(), prefix, false);
    Assert.assertEquals(0, controller.getCurrentIndex());
    Assert.assertEquals(0, controller.getLastFlushedIndex());

    controller.updateAndGet(IndexController.FLUSH_INTERVAL + 1);
    Assert.assertEquals(IndexController.FLUSH_INTERVAL + 1, controller.getCurrentIndex());
    Assert.assertEquals(IndexController.FLUSH_INTERVAL, controller.getLastFlushedIndex());

    controller = new IndexController(storageDir.getAbsolutePath(), prefix, false);
    Assert.assertEquals(IndexController.FLUSH_INTERVAL, controller.getCurrentIndex());
    Assert.assertEquals(IndexController.FLUSH_INTERVAL, controller.getLastFlushedIndex());

    controller.updateAndGet(IndexController.FLUSH_INTERVAL * 2 - 1);
    Assert.assertEquals(IndexController.FLUSH_INTERVAL * 2 - 1, controller.getCurrentIndex());
    Assert.assertEquals(IndexController.FLUSH_INTERVAL, controller.getLastFlushedIndex());

    controller = new IndexController(storageDir.getAbsolutePath(), prefix, false);
    Assert.assertEquals(IndexController.FLUSH_INTERVAL, controller.getCurrentIndex());
    Assert.assertEquals(IndexController.FLUSH_INTERVAL, controller.getLastFlushedIndex());

    controller.updateAndGet(IndexController.FLUSH_INTERVAL * 2 + 1);
    Assert.assertEquals(IndexController.FLUSH_INTERVAL * 2 + 1, controller.getCurrentIndex());
    Assert.assertEquals(IndexController.FLUSH_INTERVAL * 2, controller.getLastFlushedIndex());
  }
}
