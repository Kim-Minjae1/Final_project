document.addEventListener('DOMContentLoaded', function() {
    const weatherIcon = document.getElementById("weather_icon");
    const temp = document.getElementById("weather_now_temp");
    const tempMaxDiv = document.getElementById("weather_temp_max");
    const tempMinDiv = document.getElementById("weather_temp_min");
    
    const popDiv = document.getElementById("weather_pop");
    const humidityDiv = document.getElementById("weather_humidity");

    const currentDateTime = getCurrentDateTime();
    const key = 'K%2FCnoTIK3E6FzNpZG%2BnO6g%2FuNJgDKu1sQqq3r9bF5EExcoUhfnjuaFvlRpdpD2deCOvTeOEwJ3TUW7z8m4mKxQ%3D%3D';

    function getCurrentDateTime() {
        const now = new Date();
        let hours = now.getHours().toString().padStart(2, '0');
        const minutes = now.getMinutes();
        
        if (minutes >= 30) {
        	hours = (now.getHours() + 1).toString().padStart(2, '0');
    	}

        const baseDate = new Date(now);
        baseDate.setDate(baseDate.getDate() - 1); 

        return {
            date: `${baseDate.getFullYear()}${(baseDate.getMonth() + 1).toString().padStart(2, '0')}${baseDate.getDate().toString().padStart(2, '0')}`,
            time: '2300',
            now: hours === '24' ? `0000` : `${hours}00`
        };
    }

    function getWeather() {
        var xhr = new XMLHttpRequest();
        var url = 'http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst'; 
        var queryParams = '?' + encodeURIComponent('serviceKey') + '=' + key; 
        queryParams += '&' + encodeURIComponent('pageNo') + '=' + encodeURIComponent('1'); 
        queryParams += '&' + encodeURIComponent('numOfRows') + '=' + encodeURIComponent('300');
        queryParams += '&' + encodeURIComponent('dataType') + '=' + encodeURIComponent('JSON'); 
        queryParams += '&' + encodeURIComponent('base_date') + '=' + encodeURIComponent(currentDateTime.date);
        queryParams += '&' + encodeURIComponent('base_time') + '=' + encodeURIComponent(currentDateTime.time);
        queryParams += '&' + encodeURIComponent('nx') + '=' + encodeURIComponent('58'); 
        queryParams += '&' + encodeURIComponent('ny') + '=' + encodeURIComponent('125'); 

        xhr.open('GET', url + queryParams);
        xhr.onreadystatechange = function () {
            if (xhr.readyState == 4) {
                if (xhr.status == 200) {
                    const response = JSON.parse(xhr.responseText);
                    updateWeather(response);
                } else {
                    console.error('날씨 데이터를 가져오는 데 실패했습니다.');
                }
            }
        };
        xhr.send();
    }

    function updateWeather(data) {
        const weatherInfo = data.response.body.items.item; 
        let temperature = null;
        let tempMin = null;
        let tempMax = null;

        weatherInfo.forEach(item => {
            if(item.fcstTime === currentDateTime.now) { 
                if(item.category === 'TMP'){
                    temperature = parseFloat(item.fcstValue);
                } else if(item.category === 'PTY'){
                    if(item.fcstValue === '0'){
                        weatherInfo.forEach(innerItem => {
                            if(innerItem.category === 'SKY' && innerItem.fcstTime === currentDateTime.now) {
                                if(innerItem.fcstValue === '1') {
                                    weatherIcon.setAttribute('src', '/img/weather_sunny.png');
                                } else if(innerItem.fcstValue === '2' || innerItem.fcstValue === '3') {
                                    weatherIcon.setAttribute('src', '/img/weather_cloud.png');
                                } else{
                                    weatherIcon.setAttribute('src', '/img/weather_cloudy.png');
                                } 
                            }
                        });
                    } else if(item.fcstValue === '1'){
                        weatherIcon.setAttribute('src', '/img/weather_rainy.png');
                    } else if(item.fcstValue === '2'){
                        weatherIcon.setAttribute('src', '/img/weather_snowRainy.png');
                    } else if(item.fcstValue === '3'){
                        weatherIcon.setAttribute('src', '/img/weather_snowy.png');
                    } else{
                        weatherIcon.setAttribute('src', '/img/weather_rainy.png');
                    }
                } else if(item.category === "POP"){
					popDiv.innerText = item.fcstValue + "%";
				} else if(item.category === "REH"){
					humidityDiv.innerText = item.fcstValue + "%";
				} 
            }           
            if(item.category === 'TMN') { 
                tempMin = Math.floor(item.fcstValue);
            } else if(item.category === 'TMX') { 
                tempMax = Math.floor(item.fcstValue);
            } 
        });       
        temp.innerText = temperature + "°C";
        tempMinDiv.innerText = tempMin + "°C";
        tempMaxDiv.innerText = tempMax + "°C";
    }
    getWeather();
});
