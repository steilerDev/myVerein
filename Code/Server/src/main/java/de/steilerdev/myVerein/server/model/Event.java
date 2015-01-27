/**
 * Copyright (C) 2014 Frank Steiler <frank@steilerdev.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.steilerdev.myVerein.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.steilerdev.myVerein.server.controller.DivisionManagementController;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//Todo: Equals method

public class Event
{
    @Id
    private String id;

    @NotBlank
    private String name;
    private String description;
    private String location;
    private double locationLat;
    private double locationLog;

    @NotNull
    @JsonIgnore
    private int startDateDayOfMonth, startDateMonth, startDateYear, startDateHour, startDateMinute;
    @NotNull
    @JsonIgnore
    private int endDateDayOfMonth, endDateMonth,endDateYear, endDateHour, endDateMinute;

    @LastModifiedDate
    private Date lastChanged;

    @Transient
    private LocalDateTime startDateTime;
    @Transient
    private LocalDate startDate;

    @Transient
    private LocalDateTime endDateTime;
    @Transient
    private LocalDate endDate;

    private boolean multiDate;

    @DBRef
    @NotEmpty
    private List<Division> invitedDivision;

    public Event() {}

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public double getLocationLat()
    {
        return locationLat;
    }

    public void setLocationLat(double locationLat)
    {
        this.locationLat = locationLat;
    }

    public double getLocationLog()
    {
        return locationLog;
    }

    public void setLocationLog(double locationLog)
    {
        this.locationLog = locationLog;
    }

    public int getStartDateDayOfMonth()
    {
        return startDateDayOfMonth;
    }

    public void setStartDateDayOfMonth(int startDateDayOfMonth)
    {
        this.startDateDayOfMonth = startDateDayOfMonth;
        updateMultiDate();
    }

    public int getStartDateMonth()
    {
        return startDateMonth;
    }

    public void setStartDateMonth(int startDateMonth)
    {
        this.startDateMonth = startDateMonth;
        updateMultiDate();
    }

    public int getStartDateYear()
    {
        return startDateYear;
    }

    public void setStartDateYear(int startDateYear)
    {
        this.startDateYear = startDateYear;
        updateMultiDate();
    }

    public int getStartDateHour()
    {
        return startDateHour;
    }

    public void setStartDateHour(int startDateHour)
    {
        this.startDateHour = startDateHour;
        updateMultiDate();
    }

    public int getStartDateMinute()
    {
        return startDateMinute;
    }

    public void setStartDateMinute(int startDateMinute)
    {
        this.startDateMinute = startDateMinute;
        updateMultiDate();
    }

    public int getEndDateDayOfMonth()
    {
        return endDateDayOfMonth;
    }

    public void setEndDateDayOfMonth(int endDateDayOfMonth)
    {
        this.endDateDayOfMonth = endDateDayOfMonth;
        updateMultiDate();
    }

    public int getEndDateMonth()
    {
        return endDateMonth;
    }

    public void setEndDateMonth(int endDateMonth)
    {
        this.endDateMonth = endDateMonth;
        updateMultiDate();
    }

    public int getEndDateYear()
    {
        return endDateYear;
    }

    public void setEndDateYear(int endDateYear)
    {
        this.endDateYear = endDateYear;
        updateMultiDate();
    }

    public int getEndDateHour()
    {
        return endDateHour;
    }

    public void setEndDateHour(int endDateHour)
    {
        this.endDateHour = endDateHour;
        updateMultiDate();
    }

    public int getEndDateMinute()
    {
        return endDateMinute;
    }

    public void setEndDateMinute(int endDateMinute)
    {
        this.endDateMinute = endDateMinute;
        updateMultiDate();
    }

    public Date getLastChanged()
    {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged)
    {
        this.lastChanged = lastChanged;
    }

    public LocalDateTime getStartDateTime()
    {
        startDateTime = LocalDateTime.of(startDateYear, startDateMonth, startDateDayOfMonth, startDateHour, startDateMinute);
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime)
    {
        this.startDateTime = startDateTime;
        if(startDateTime != null)
        {
            startDateYear = startDateTime.getYear();
            startDateMonth = startDateTime.getMonthValue();
            startDateDayOfMonth = startDateTime.getDayOfMonth();
            startDateHour = startDateTime.getHour();
            startDateMinute = startDateTime.getMinute();
        }
        updateMultiDate();
    }

    public LocalDateTime getEndDateTime()
    {
        endDateTime = LocalDateTime.of(endDateYear, endDateMonth, endDateDayOfMonth, endDateHour, endDateMinute);
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime)
    {
        this.endDateTime = endDateTime;
        if(endDateTime != null)
        {
            endDateYear = endDateTime.getYear();
            endDateMonth = endDateTime.getMonthValue();
            endDateDayOfMonth = endDateTime.getDayOfMonth();
            endDateHour = endDateTime.getHour();
            endDateMinute = endDateTime.getMinute();
        }
        updateMultiDate();
    }

    public List<Division> getInvitedDivision()
    {
        return invitedDivision;
    }

    /**
     * The function is setting all invited divisions, but is optimizing the set by eliminating unnecessary divisions.
     * @param invitedDivision
     */
    public void setInvitedDivision(List<Division> invitedDivision)
    {
        if(invitedDivision != null)
        {
            this.invitedDivision = DivisionManagementController.getOptimizedSetOfDivisions(invitedDivision);
        } else
        {
            this.invitedDivision = invitedDivision;
        }
    }

    public void addDivision(Division division)
    {
        if(invitedDivision == null)
        {
            invitedDivision = new ArrayList<>();
        }
        invitedDivision.add(division);
    }

    public boolean isMultiDate()
    {
        return multiDate;
    }

    public void setMultiDate(boolean multiDate)
    {
        this.multiDate = multiDate;
    }

    public void updateMultiDate()
    {
        multiDate = (startDateDayOfMonth != endDateDayOfMonth) || (startDateMonth != endDateMonth) || (startDateYear != endDateYear);
    }

    public LocalDate getStartDate()
    {
        startDate = getStartDateTime().toLocalDate();
        return startDate;
    }

    public void setStartDate(LocalDate startDate)
    {
        this.startDate = startDate;
    }

    public LocalDate getEndDate()
    {
        endDate = getEndDateTime().toLocalDate();
        return endDate;
    }

    public void setEndDate(LocalDate endDate)
    {
        this.endDate = endDate;
    }
}
