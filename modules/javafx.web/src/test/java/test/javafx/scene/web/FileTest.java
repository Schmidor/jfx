/*
 * Copyright (c) 2018, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package test.javafx.scene.web;

import com.sun.javafx.webkit.UIClientImplShim;
import com.sun.webkit.WebPage;
import com.sun.webkit.WebPageShim;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngineShim;
import netscape.javascript.JSObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;

import static javafx.concurrent.Worker.State.SUCCEEDED;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FileTest extends TestBase {
    private final WebPage page = WebEngineShim.getPage(getEngine());
    private String[] fileList = { new File("src/test/resources/test/html/HelloWorld.txt").getAbsolutePath() };

    private State getLoadState() {
        return submit(() -> getEngine().getLoadWorker().getState());
    }

    private String getScriptString(String api) {
        String scriptContent = String.format("<script type='text/javascript'>" +
                                    "var result;" +
                                    "window.addEventListener('click', (e) => {" +
                                        "document.getElementById('file').click();" +
                                    "});" +
                                    "function readFile()" +
                                    "{" +
                                        "file = event.target.files[0];" +
                                        "result = file." + api + ";" +
                                    "}" +
                               "</script>" +
                               "<body> <input type='file' id='file' onchange='readFile()'/></body>");
        return scriptContent;
    }

    @Before
    public void before() {
        UIClientImplShim.test_setChooseFiles(fileList);
    }

    private void loadFileReaderTestScript(String testScript) {
        loadContent(testScript);
        submit(() -> {
            // we send a dummy mouse click event at (0,0) to simulate click on file chooser button.
            WebPageShim.click(page, 0, 0);
        });
    }

    @Test public void testFileName() {
        loadFileReaderTestScript(getScriptString("name"));
        submit(() -> {
            assertEquals("Unexpected file name received", "HelloWorld.txt", getEngine().executeScript("window.result"));
        });
    }
}
