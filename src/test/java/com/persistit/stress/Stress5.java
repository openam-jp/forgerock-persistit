/**
 * Copyright © 2005-2012 Akiban Technologies, Inc.  All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, version 3 (only) of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * This program may also be available under different license terms. For more
 * information, see www.akiban.com or contact licensing@akiban.com.
 */

package com.persistit.stress;

import com.persistit.Exchange;
import com.persistit.Key;
import com.persistit.Value;
import com.persistit.exception.PersistitException;
import com.persistit.test.TestResult;
import com.persistit.util.ArgParser;

public class Stress5 extends StressBase {
    
    public Stress5(String argsString) {
        super(argsString);
    }

    private final static String[] ARGS_TEMPLATE = { "repeat|int:1:0:1000000000|Repetitions",
            "count|int:10000:0:1000000000|Number of nodes to populate", "size|int:4029:10:10000000|Data record size",
            "splay0|int:12:1:1000|Splay 0", "splay1|int:3:1:1000|Splay 1", "splay2|int:7:1:1000|Splay 2", };

    int _splay0;
    int _splay1;
    int _splay2;
    int _size;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        _ap = new ArgParser("com.persistit.Stress5", _args, ARGS_TEMPLATE);
        _total = _ap.getIntValue("count");
        _repeatTotal = _ap.getIntValue("repeat");
        _total = _ap.getIntValue("count");

        _splay0 = _ap.getIntValue("splay0");
        _splay1 = _ap.getIntValue("splay1");
        _splay2 = _ap.getIntValue("splay2");
        _size = _ap.getIntValue("size");

        try {
            // Exchange with Thread-private Tree
            _ex = getPersistit().getExchange("persistit", _rootName + _threadIndex, true);
            _exs = getPersistit().getExchange("persistit", "shared", true);
        } catch (final Exception ex) {
            handleThrowable(ex);
        }
    }

    /**
     * Implements tests with long keys and values of borderline length
     */
    @Override
    public void executeTest() {
        final Value value = new Value(getPersistit());

        final int baselineCount = 500;
        final int keyLength = 2041;
        final int maxDepth = keyLength - 10;
        final int minDepth = 20;

        for (_repeat = 0; (_repeat < _repeatTotal || isUntilStopped()) && !isStopped(); _repeat++) {
            try {

                setPhase("@");
                _ex.clear().remove(Key.GTEQ);
                setPhase("a");
                for (_count = 0; (_count < baselineCount) && !isStopped(); _count++) {
                    setupKey(_ex, keyLength, keyLength - 5, _count, _count, '5');
                    setupTestValue(_ex, _count, _size);
                    _ex.store();
                }
                if (isStopped()) {
                    break;
                }
                setPhase("b");
                int depth;
                for (_count = 0, depth = maxDepth; (depth > minDepth) && !isStopped(); depth -= _splay1, _count++) {
                    setupKey(_ex, keyLength, depth, minDepth + (depth % _splay0), 55555 + depth, '5');
                    setupTestValue(_ex, 55555 + depth, _size);
                    _ex.store();
                }
                if (isStopped()) {
                    break;
                }

                setPhase("c");
                for (_count = 0, depth = maxDepth; (depth > minDepth) && !isStopped(); depth -= _splay2, _count++) {
                    setupKey(_ex, keyLength, depth, minDepth + (depth % _splay0), 55555 - depth, '5');
                    setupTestValue(_ex, 55555 - depth, _size);
                    _ex.store();
                }
                if (isStopped()) {
                    break;
                }

                setPhase("d");
                for (_count = 0, depth = maxDepth; (depth > minDepth) && !isStopped(); depth -= _splay1, _count++) {
                    setupKey(_ex, keyLength, depth, minDepth + (depth % _splay0), 55555 + depth, '5');
                    setupTestValue(_ex, 55555 + depth, _size);
                    _ex.fetch(value);
                    compareValues(_ex.getValue(), value);
                    if (isStopped()) {
                        break;
                    }
                    _ex.remove();
                }
                if (isStopped()) {
                    break;
                }

                setPhase("e");
                for (_count = 0, depth = maxDepth; (depth > minDepth) && !isStopped(); depth -= _splay2, _count++) {
                    setupKey(_ex, keyLength, depth, minDepth + (depth % _splay0), 55555 - depth, '5');
                    setupTestValue(_ex, 55555 - depth, _size);
                    _ex.fetch(value);
                    compareValues(_ex.getValue(), value);
                    if (isStopped()) {
                        break;
                    }
                    if (!_ex.remove()) {
                        _result = new TestResult(false, "Failed to remove depth=" + depth);
                        forceStop();
                        break;
                    }
                }
                if (isStopped()) {
                    break;
                }

                setPhase("f");
                for (_count = 0; _count < baselineCount; _count++) {
                    setupKey(_ex, keyLength, keyLength - 5, _count, _count, '5');
                    if (!_ex.remove()) {
                        System.out.println("Failed to remove counter=" + _count);
                    }
                }

                setPhase("g");
                _ex.clear();
                if (_ex.traverse(Key.GT, true)) {
                    System.out.println("Tree is not empty");
                }

            } catch (final PersistitException de) {
                handleThrowable(de);
            }
        }
    }

    private void setupKey(final Exchange ex, final int length, final int depth, final int a, final int b,
            final char fill) {
        _sb1.setLength(0);
        for (int i = 0; i < length; i++) {
            _sb1.append(fill);
        }
        fillLong(b, depth, 5, true);
        ex.clear().append(a).append(_sb1);
    }

}
