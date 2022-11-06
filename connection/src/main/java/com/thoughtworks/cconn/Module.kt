/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn

import com.thoughtworks.cconn.log.Logger

/**
 * Connection module base
 */
interface Module {
    /**
     * Set logger
     *
     * @param logger log handler
     */
    fun setLogger(logger: Logger)
}