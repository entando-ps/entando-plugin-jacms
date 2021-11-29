/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package org.entando.entando.plugins.jacms.apsadmin.content.bulk.report;

import java.util.Date;
import java.util.List;

/**
 * The report of a bulk {@link ApsCommand}
 * @author E.Mezzano
 *
 * @param <I> The type of items on which the command is applied.
 */
public interface BulkCommandReport<I> {
	
	/**
	 * Returns the number items onto apply the command.
	 * @return The number items onto apply the command.
	 */
	public int getTotal();

	/**
	 * Returns the number items onto the command is applied.
	 * @return The number items onto the command is applied.
	 */
	public int getApplyTotal();

	/**
	 * Returns the number items onto the command is succesfully applied.
	 * @return The number items onto the command is succesfully applied.
	 */
	public int getApplySuccesses();

	/**
	 * Returns the number items onto the command is applied with errors.
	 * @return The number items onto the command is applied with errors.
	 */
	public int getApplyErrors();

	/**
	 * Returns the succeeded items of the command.
	 * @return The succeeded items of the command.
	 */
	public List<I> getSuccesses();

	/**
	 * Returns the instant of the end of the command.
	 * @return The instant of the end of the command.
	 */
	public Date getEndingTime();

}
