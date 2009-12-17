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
 * Created on May 3, 2005
 */
package com.persistit.logging;

import org.apache.commons.logging.Log;

/**
 * Wraps an <tt>org.apache.commons.logging.Log</tt> for Persistit logging.
 * Code to enable default logging through Apache commons logging is shown here:
 * <code><pre>
 *    // refer to any appropriate org.apache.commons.logging.Log, for example
 *    Log log = LogFactory.getLog(getClass());
 *    Persistit.setPersistitLogger(new ApacheCommonsAdapter(logger));
 * </pre></code>
 * @version 1.1
 */
public class ApacheCommonsLogAdapter extends AbstractPersistitLogger
{
    /**
     * The Log object wrapped by this adapter.
     */
    private Log _log;
    
    /**
     * Wraps an Apache commons <tt>Log</tt> so that Persistit can write to it. 
     * @param log   A <tt>Log</tt> to which Persistit log messages
     *              will be directed.
     */
    public ApacheCommonsLogAdapter(Log log)
    {
        _log = log;
    }

    /**
     * Overrides <tt>isLoggable</tt> it allow control by the 
     * wrapped <tt>Log</tt>.
     * @param lt    The <tt>LogTemplate</tt>
     */
    public boolean isLoggable(LogTemplate lt)
    {
        int level = lt.getLevel();
        switch (level)
        {
            case AbstractPersistitLogger.FINEST:
                return _log.isTraceEnabled();

            case AbstractPersistitLogger.FINER:
                return _log.isTraceEnabled();
            
            case AbstractPersistitLogger.FINE:
                return _log.isDebugEnabled();
            
            case AbstractPersistitLogger.INFO:
                return _log.isInfoEnabled();
                
            case AbstractPersistitLogger.WARNING:
                return _log.isWarnEnabled();
            
            case AbstractPersistitLogger.SEVERE:
                return _log.isErrorEnabled();
            
            case AbstractPersistitLogger.ALWAYS:
                return _log.isFatalEnabled();
            
            default:
                return true;
        }
    }

    /**
     * Writes a log message generated by Persistit to the wrapped
     * <tt>Log</tt>.
     * @param lt        The <tt>LogTemplate</tt>
     * @param message   The message to write to the log.
     */
    public void log(LogTemplate lt, String message)
    {
        int level = lt.getLevel();
        switch (level)
        {
            case AbstractPersistitLogger.FINEST:
                _log.trace(message);
                break;

            case AbstractPersistitLogger.FINER:
                _log.trace(message);
                break;
            
            case AbstractPersistitLogger.FINE:
                _log.debug(message);
                break;
            
            case AbstractPersistitLogger.INFO:
                _log.info(message);
                break;
                
            case AbstractPersistitLogger.WARNING:
                _log.warn(message);
                break;
            
            case AbstractPersistitLogger.SEVERE:
                _log.error(message);
                break;
            
            case AbstractPersistitLogger.ALWAYS:
                _log.fatal(message);
                break;
            
            default:
                _log.fatal(message);
                break;
        }
    }

}
