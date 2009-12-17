/*
 * Copyright (c) 2004 Persistit Corporation. All Rights Reserved.
 *
 * The Java source code is the confidential and proprietary information
 * of Persistit Corporation ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Persistit Corporation.
 *
 * PERSISTIT CORPORATION MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. PERSISTIT CORPORATION SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * Created on May 19, 2005
 */
package com.persistit.ui;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
/**
 * This is just enough of a ClassLoader implementation to load the
 * JavaHelp classes from a nested Jar file.  The purpose here is to allow
 * jhbasic.jar to reside unchanged (due to license constraints) inside
 * the persistit_jsa110_ui.jar file.  When the user clicks on Help the first
 * time, AdminUI constructs an InnerJarClassLoader and calls addJar to
 * load the contents of inner jar files.  The findClass and getResourceAsStream
 * methods then look up the loaded resources when called.  This class keeps
 * a Map containing all the resources packed in the JAR files, so it is good
 * to unload this from memory when possible.
 * @author Peter Beaman
 * @version 1.0
 */
class InnerJarClassLoader 
extends ClassLoader
{
    HashMap _resourceMap = new HashMap();
    
    InnerJarClassLoader(ClassLoader parent)
    throws IOException
    {
        super(parent);
    }
    
    public void addJar(String jarName)
    throws IOException
    {
        addJar(getClass().getClassLoader().getResourceAsStream(jarName));
    }
    
    public void addJar(InputStream is)
    throws IOException
    {
        JarInputStream jis = 
            new JarInputStream(
                new BufferedInputStream(is, 32768));
        
        byte[] hbytes = new byte[65536];
        for (;;)
        {
            ZipEntry entry = jis.getNextEntry();
            if (entry == null) break;
            String name = entry.getName();
            int offset = 0;
            for (;;)
            {
                int length = jis.read(hbytes, offset, hbytes.length - offset);
                if (length == -1) break;
                offset += length;
                if (offset == hbytes.length)
                {
                    byte[] temp = new byte[hbytes.length + 65536];
                    System.arraycopy(hbytes, 0, temp, 0, offset);
                    hbytes= temp;
                }
            }
            byte[] bytes = new byte[offset];
            System.arraycopy(hbytes, 0, bytes, 0, offset);
            _resourceMap.put(name, bytes);
            jis.closeEntry();
        }
    }
    
    public Class findClass(String className)
    throws ClassNotFoundException
    {
        String fileName = className.replace('.', '/') + ".class";
        byte[] bytes = (byte[])_resourceMap.get(fileName);
        if (bytes != null)
        {
            return defineClass(className, bytes, 0, bytes.length);
        }
        else throw new ClassNotFoundException(className);
    }
    
    public InputStream getResourceAsStream(String name)
    {
        byte[] bytes= (byte[])_resourceMap.get(name);
        if (bytes != null) return new ByteArrayInputStream(bytes);
        else return null;
    }
    
}
