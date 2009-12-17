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
 */

package com.persistit.stress;

import com.persistit.ArgParser;
import com.persistit.Exchange;
import com.persistit.Key;
import com.persistit.Persistit;
import com.persistit.Transaction;
import com.persistit.TransactionRunnable;
import com.persistit.Value;
import com.persistit.exception.PersistitException;
import com.persistit.test.TestRunner;

public class Stress1txn extends StressBase {

    private final static String SHORT_DESCRIPTION =
        "Simple transactional write/read/delete/traverse loops";

    private final static String LONG_DESCRIPTION =
        "   Simple stress test that perform <repeat> iterations of the following: \r\n"
            + "    - insert <count> sequentially ascending keys \r\n"
            + "    - read and verify <count> sequentially ascending key/value pairs\r\n"
            + "    - traverse and count all keys using next() \r\n"
            + "    - delete <count> sequentially ascending keys \r\n"
            + "   Optional <splay> value allows variations in key sequence: \r\n"
            + "   Same as Stress1 except uses Transactions\r\n";

    private final static String[] ARGS_TEMPLATE =
        { "op|String:wrtd|Operations to perform",
            "repeat|int:1:0:1000000000|Repetitions",
            "count|int:10000:0:1000000000|Number of nodes to populate",
            "size|int:30:1:20000|Size of each data value",
            "splay|int:1:1:1000|Splay", };

    int _size;
    int _splay;
    String _opflags;

    @Override
    public String shortDescription() {
        return SHORT_DESCRIPTION;
    }

    @Override
    public String longDescription() {
        return LONG_DESCRIPTION;
    }

    @Override
    public void setupTest(final String[] args) {
        _ap = new ArgParser("com.persistit.Stress1", args, ARGS_TEMPLATE);
        _splay = _ap.getIntValue("splay");
        _opflags = _ap.getStringValue("op");
        _size = _ap.getIntValue("size");
        _repeatTotal = _ap.getIntValue("repeat");
        _total = _ap.getIntValue("count");
        _dotGranularity = 10000;

        try {
            // Exchange with Thread-private Tree
            _ex = getPersistit().getExchange("persistit", _rootName + _threadIndex, true);
        } catch (final Exception ex) {
            handleThrowable(ex);
        }
    }

    @Override
    public void runTest() {
        final Value value1 = _ex.getValue();
        final Value value2 = new Value(getPersistit());
        final Transaction txn = getPersistit().getTransaction();

        setPhase("@");
        txn.begin();
        try {
            _ex.clear().remove(Key.GTEQ);
            txn.commit();
        } catch (final Exception e) {
            handleThrowable(e);
        } finally {
            txn.end();
        }
        println();
        for (_repeat = 0; (_repeat < _repeatTotal) && !isStopped(); _repeat++) {
            println();
            println("Starting cycle " + (_repeat + 1) + " of " + _repeatTotal);

            if (_opflags.indexOf('w') >= 0) {
                setPhase("w");
                txn.begin();
                try {
                    for (_count = 0; (_count < _total) && !isStopped(); _count++) {
                        dot();
                        final int keyInteger = keyInteger(_count);
                        _ex.clear().append(keyInteger);
                        setupTestValue(_ex, _count, _size);
                        _ex.store();
                    }
                    txn.commit();
                } catch (final Exception e) {
                    handleThrowable(e);
                    break;
                } finally {
                    txn.end();
                }
            }

            if (_opflags.indexOf('r') >= 0) {
                setPhase("r");
                for (_count = 0; (_count < _total) && !isStopped(); _count++) {
                    dot();
                    final int keyInteger = keyInteger(_count);
                    _ex.clear().append(keyInteger);
                    setupTestValue(_ex, _count, _size);
                    try {
                        txn.run(new TransactionRunnable() {
                            public void runTransaction()
                                throws PersistitException {

                                // fetch to a different Value object so we can
                                // compare
                                // with the original.
                                _ex.fetch(value2);
                                compareValues(value1, value2);
                            }
                        });
                    } catch (final Exception e) {
                        handleThrowable(e);
                    }
                }
            }

            if (_opflags.indexOf('t') >= 0) {
                setPhase("t");
                txn.begin();
                try {
                    _ex.clear().append(Integer.MIN_VALUE);
                    for (_count = 0; (_count < (_total * 10)) && !isStopped(); _count++) {
                        dot();
                        if (!_ex.next()) {
                            break;
                        }
                    }
                    if (_count != _total) {
                        _result =
                            new TestRunner.Result(false, "Traverse count="
                                + _count + " out of " + _total + " repetition="
                                + _repeat + " in thread=" + _threadIndex);
                        println(_result);
                        forceStop();
                        break;
                    }
                    txn.commit();
                } catch (final Exception e) {
                    handleThrowable(e);
                } finally {
                    txn.end();
                }
            }

            if (_opflags.indexOf('d') >= 0) {
                setPhase("d");
                txn.begin();
                try {
                    for (_count = 0; (_count < _total) && !isStopped(); _count++) {
                        dot();
                        final int keyInteger = keyInteger(_count);
                        _ex.clear().append(keyInteger);
                        _ex.remove();
                    }
                    txn.commit();
                } catch (final Exception e) {
                    handleThrowable(e);
                } finally {
                    txn.end();
                }
            }

            if ((_opflags.indexOf('h') > 0) && !isStopped()) {
                setPhase("h");
                try {
                    Thread.sleep(1000);
                } catch (final Exception e) {
                }
            }

        }
        println();
        print("done");
    }

    int keyInteger(final int counter) {
        int keyInteger = (counter * _splay) % _total;
        if (keyInteger < 0) {
            keyInteger += _total;
        }
        return keyInteger;
    }

    public static void main(final String[] args) throws Exception {
        final Stress1txn test = new Stress1txn();
        test.runStandalone(args);
    }
}
