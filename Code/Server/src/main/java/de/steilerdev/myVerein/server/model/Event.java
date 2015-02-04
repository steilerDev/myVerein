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
import com.fasterxml.jackson.annotation.JsonInclude;
import de.steilerdev.myVerein.server.controller.DivisionManagementController;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Event
{
    @Id
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @NotBlank
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String location;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private double locationLat;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private double locationLng;

    @NotNull
    @JsonIgnore
    private int startDateDayOfMonth, startDateMonth, startDateYear, startDateHour, startDateMinute;
    @NotNull
    @JsonIgnore
    private int endDateDayOfMonth, endDateMonth,endDateYear, endDateHour, endDateMinute;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime lastChanged = LocalDateTime.now();

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime startDateTime;
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate startDate;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime endDateTime;
    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate endDate;

    private boolean multiDate;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String administrationNotAllowedMessage;

    @DBRef
    @NotEmpty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Division> invitedDivision;

    @DBRef
    @NotNull
    private User eventAdmin;

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
        this.description = description != null && !description.isEmpty()? description: null;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location != null && !location.isEmpty()? location: null;
    }

    public double getLocationLat()
    {
        return locationLat;
    }

    public void setLocationLat(double locationLat)
    {
        this.locationLat = locationLat;
    }

    public double getLocationLng()
    {
        return locationLng;
    }

    public void setLocationLng(double locationLng)
    {
        this.locationLng = locationLng;
    }

    public int getStartDateDayOfMonth()
    {
        return startDateDayOfMonth;
    }

    public void setStartDateDayOfMonth(int startDateDayOfMonth)
    {
        this.startDateDayOfMonth = startDateDayOfMonth;
    }

    public int getStartDateMonth()
    {
        return startDateMonth;
    }

    public void setStartDateMonth(int startDateMonth)
    {
        this.startDateMonth = startDateMonth;
    }

    public int getStartDateYear()
    {
        return startDateYear;
    }

    public void setStartDateYear(int startDateYear)
    {
        this.startDateYear = startDateYear;
    }

    public int getStartDateHour()
    {
        return startDateHour;
    }

    public void setStartDateHour(int startDateHour)
    {
        this.startDateHour = startDateHour;
    }

    public int getStartDateMinute()
    {
        return startDateMinute;
    }

    public void setStartDateMinute(int startDateMinute)
    {
        this.startDateMinute = startDateMinute;
    }

    public int getEndDateDayOfMonth()
    {
        return endDateDayOfMonth;
    }

    public void setEndDateDayOfMonth(int endDateDayOfMonth)
    {
        this.endDateDayOfMonth = endDateDayOfMonth;
    }

    public int getEndDateMonth()
    {
        return endDateMonth;
    }

    public void setEndDateMonth(int endDateMonth)
    {
        this.endDateMonth = endDateMonth;
    }

    public int getEndDateYear()
    {
        return endDateYear;
    }

    public void setEndDateYear(int endDateYear)
    {
        this.endDateYear = endDateYear;
    }

    public int getEndDateHour()
    {
        return endDateHour;
    }

    public void setEndDateHour(int endDateHour)
    {
        this.endDateHour = endDateHour;
    }

    public int getEndDateMinute()
    {
        return endDateMinute;
    }

    public void setEndDateMinute(int endDateMinute)
    {
        this.endDateMinute = endDateMinute;
    }

    public LocalDateTime getLastChanged()
    {
        return lastChanged;
    }

    public void setLastChanged(LocalDateTime lastChanged)
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
        this.invitedDivision = invitedDivision;
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

    /**
     * This function updates the multi date flag, depending on the start and end date.
     */
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

    public User getEventAdmin()
    {
        return eventAdmin;
    }

    public void setEventAdmin(User eventAdmin)
    {
        this.eventAdmin = eventAdmin;
    }

    public String getAdministrationNotAllowedMessage()
    {
        return administrationNotAllowedMessage;
    }

    public void setAdministrationNotAllowedMessage(String administrationNotAllowedMessage)
    {
        this.administrationNotAllowedMessage = administrationNotAllowedMessage;
    }

    /**
     * This function is removing unnecessary divisions from the invited division set.
     */
    public void optimizeInvitedDivisionSet()
    {
        if(invitedDivision != null && !invitedDivision.isEmpty())
        {
            invitedDivision = DivisionManagementController.getOptimizedSetOfDivisions(invitedDivision);
        }
    }

    @Override
    public int hashCode()
    {
        return id == null? 0: id.hashCode();
    }

    /**
     * Equality is based on the same id String within the database.
     * @param obj The object compared to the current object.
     * @return True if the IDs of the two objects
     */
    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj instanceof Event && this.id != null && this.id.equals(((Event) obj).getId());
    }
}
