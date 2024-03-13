export const dateToShortDateStr = (date) => {
    let day = date.getDate();
    let month = date.getMonth() + 1;
    const year = date.getFullYear();

    day = day < 10 ? '0' + day : day;
    month = month < 10 ? '0' + month : month;

    return `${day}.${month}.${year}`;
}

export const isoDateStrToShortDateStr = (dateStr) => {
    if (dateStr == null){
        return ""
    }
    const parts = dateStr.split('-');

    const convertedDate = `${parts[2]}.${parts[1]}.${parts[0]}`;

    return convertedDate;
}

export const isoDateTimeStrToShortDateStr = (dateStr) => {



    const dateObj = new Date(dateStr);

    if (dateStr == null){
        return ""
    }

    // Extract the day, month, and year from the Date object
    const day = String(dateObj.getDate()).padStart(2, '0');
    const month = String(dateObj.getMonth() + 1).padStart(2, '0'); // January is 0!
    const year = dateObj.getFullYear();

    // Format the date as dd.mm.yyyy
    const formattedDate = `${day}.${month}.${year}`;

    return formattedDate;
}