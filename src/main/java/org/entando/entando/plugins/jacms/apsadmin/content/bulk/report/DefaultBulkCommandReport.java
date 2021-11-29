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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.HashedMap;
import org.entando.entando.plugins.jacms.apsadmin.content.bulk.commands.ApsCommandErrorCode;


/**
 * The default report for a bulk {@link ApsCommand}.
 * It's a wrapper of the {@link BaseBulkCommand} and of its {@link BulkCommandTracer}.
 * @author E.Mezzano
 *
 * @param <I> The type of items on which the command is applied.
 */
public class DefaultBulkCommandReport<I> implements BulkCommandReport<I> {
    
    private int applyTotal = 0;
    private int total = 0;
    private int applySuccesses = 0;
    private int applyErrors = 0;
    private Date endingTime;
    private List<I> successes = new ArrayList<>();
    private Map<I, ApsCommandErrorCode> errors = new HashedMap<>();
    
    /*
    @Override
    public int getTotal() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getApplyTotal() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getApplySuccesses() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getApplyErrors() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<I> getSuccesses() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Date getEndingTime() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    */
    
    
    
	/*
	public DefaultBulkCommandReport(BaseBulkCommand<I, ?, ?> command) {
		this._command = command;
	}

	@Override
	public String getCommandId() {
		return this._command.getId();
	}

	@Override
	public int getTotal() {
		return this._command.getTotal();
	}

	@Override
	public int getApplyTotal() {
		return this._command.getApplySuccesses() + this._command.getApplyErrors();
	}

	@Override
	public int getApplySuccesses() {
		return this._command.getApplySuccesses();
	}

	@Override
	public int getApplyErrors() {
		return this._command.getApplyErrors();
	}

	@Override
	public List<I> getSuccesses() {
		return this._command.getTracer().getSuccesses();
	}
    
	@Override
	public Map<I, ApsCommandWarningCode> getWarnings() {
		return this._command.getTracer().getWarnings();
	}

	@Override
	public Map<I, ApsCommandErrorCode> getErrors() {
		return this._command.getTracer().getErrors();
	}

	@Override
	public ApsCommandStatus getStatus() {
		return this._command.getStatus();
	}
    
	@Override
	public Date getEndingTime() {
		return this._command.getEndingTime();
	}
	
	private BaseBulkCommand<I, ?, ?> _command;
    */

    @Override
    public int getApplyTotal() {
        return applyTotal;
    }
    public void setApplyTotal(int applyTotal) {
        this.applyTotal = applyTotal;
    }
    
    @Override
    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public int getApplySuccesses() {
        return applySuccesses;
    }

    public void setApplySuccesses(int applySuccesses) {
        this.applySuccesses = applySuccesses;
    }

    @Override
    public int getApplyErrors() {
        return applyErrors;
    }

    public void setApplyErrors(int applyErrors) {
        this.applyErrors = applyErrors;
    }

    @Override
    public Date getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(Date endingTime) {
        this.endingTime = endingTime;
    }

    @Override
    public List<I> getSuccesses() {
        return successes;
    }
    public void setSuccesses(List<I> successes) {
        this.successes = successes;
    }

    public Map<I, ApsCommandErrorCode> getErrors() {
        return errors;
    }

    public void setErrors(Map<I, ApsCommandErrorCode> errors) {
        this.errors = errors;
    }
    
}
