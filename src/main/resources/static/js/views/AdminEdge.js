import {
  optionTemplate,
  subwayLinesItemTemplate
} from "../../utils/templates.js";
import { defaultSubwayLines } from "../../utils/subwayMockData.js";
import tns from "../../lib/slider/tiny-slider.js";
import { EVENT_TYPE } from "../../utils/constants.js";
import Modal from "../../ui/Modal.js";
import api from "../../api/index.js";

function AdminEdge() {
  const $subwayLinesSlider = document.querySelector(".subway-lines-slider");
  const $selectStation = document.querySelector("#station-select-options");
  const $preStation = document.querySelector("#depart-station-name");
  const $currentStation = document.querySelector("#arrival-station-name");
  const $createLineStationButton = document.querySelector("#submit-button");

  const createSubwayEdgeModal = new Modal();

  const onCreateSubwayPreStation = async () => {
    await api.station.getId($preStation.value).then(data => {
      return data;
    });
  }

  const onCreateSubwayCurrentStation = async () => {
    await api.station.getId($currentStation.value).then(data => {
      return data;
    });
  }

  const onCreateSubwayEdge = event => {
    let preStationId = onCreateSubwayPreStation();
    let currentStationId = onCreateSubwayCurrentStation();

    console.log("preStationId : " + preStationId);
    console.log("currentStationId : " + currentStationId);

    const $target = event.target;
    let lineId = $target.dataset.edgeId;

    console.log("lineId : " + lineId);

    const newSubwayEdge = {
      preStationId: preStationId,
      stationId: currentStationId,
      distance: 10,
      duration: 10
    }

    api.lines.createLineStation(lineId, newSubwayEdge);

    $selectStation.value = "";
    $preStation.value = "";
    $currentStation.value = "";
  }

  const initSubwayLinesSlider = async () => {
    await api.lines.get().then(data => {
      $subwayLinesSlider.innerHTML = data.map(line => subwayLinesItemTemplate(line))
          .join("");
    });

    tns({
      container: ".subway-lines-slider",
      loop: true,
      slideBy: "page",
      speed: 400,
      autoplayButtonOutput: false,
      mouseDrag: true,
      lazyload: true,
      controlsContainer: "#slider-controls",
      items: 1,
      edgePadding: 25
    });
  };

  const initSubwayLineOptions = async () => {
    let subwayLineOptionTemplate;
    await api.lines.get().then(data => {
      subwayLineOptionTemplate = data.map(line => optionTemplate(line)).join("");
    })

    const $stationSelectOptions = document.querySelector(
      "#station-select-options"
    );
    $stationSelectOptions.insertAdjacentHTML(
      "afterbegin",
      subwayLineOptionTemplate
    );
  };

  const onRemoveStationHandler = event => {
    const $target = event.target;
    const isDeleteButton = $target.classList.contains("mdi-delete");
    if (isDeleteButton) {
      $target.closest(".list-item").remove();
    }
  };

  const initEventListeners = () => {
    $subwayLinesSlider.addEventListener(EVENT_TYPE.CLICK, onRemoveStationHandler);
    $createLineStationButton.addEventListener(EVENT_TYPE.CLICK, onCreateSubwayEdge);
  };

  this.init = () => {
    initSubwayLinesSlider();
    initSubwayLineOptions();
    initEventListeners();
  };
}

const adminEdge = new AdminEdge();
adminEdge.init();
