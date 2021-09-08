<template>
	<div>
		<el-container>
			<el-main>
				<el-card>
					<div slot="header" style="text-align: left">
						<span><b>{{train}}次列车时刻表</b></span>
						<el-button :style="{float: 'right', padding: '3px 0', color: editing?editingStyle:''}" @mouseover.native="editingStyle='salmon'" @mouseleave.native="editingStyle='red'" @mousedown.native="editingStyle='crimson'" type="text" @click="editing?submit():editing=true;">{{editing?'提交':'编辑'}}</el-button>
					</div>
					<div>
						<el-table :data="trainRoutes" v-loading="loading" stripe border>
							<el-table-column prop="index" label="停靠站序" width="100">
							</el-table-column>
							<el-table-column prop="station" label="到达车站">
							</el-table-column>
							<el-table-column prop="arrive" label="进站时间">
							</el-table-column>
							<el-table-column prop="leave" label="发车时间">
							</el-table-column>
							<el-table-column prop="stop" label="停留时间">
							</el-table-column>
							<el-table-column prop="days" label="发车天数">
							</el-table-column>
							<el-table-column prop="distance" label="总里程数">
							</el-table-column>
							<el-table-column v-if="editing" label="操作" width="290">
								<template slot-scope="scope">
									<el-button type="primary" @click="upMv(scope.$index)"><i class="el-icon-caret-top"></i></el-button>
									<el-button type="success" @click="dnMv(scope.$index)"><i class="el-icon-caret-bottom"></i></el-button>
									<el-button type="info" @click="modify(scope.$index)"><i class="el-icon-edit"></i></el-button>
									<el-button type="danger" @click="rm(scope.$index)"><i class="el-icon-delete-solid"></i></el-button>
								</template>
							</el-table-column>
						</el-table>
						<div slot="append" v-if="editing">
							<el-button @click="isAdding=true;" style="width: 100%; margin-bottom: 10px" plain><i class="el-icon-plus"></i></el-button>
						</div>
					</div>
				</el-card>
			</el-main>
		</el-container>

		<div>
			<el-dialog title="新增站台" :visible.sync="isAdding" width="30%" @open="$refs.addForm?$refs.addForm.resetFields():''">
				<el-form :model="addForm" ref="addForm" :rules="addRules">
					<el-form-item prop="station" label="到达车站">
						<el-autocomplete placeholder="请输入车站名称" v-model="addForm.station" :fetch-suggestions="querySearch" style="width: 50%"></el-autocomplete>
					</el-form-item>
					<el-form-item prop="arrive" label="进站时间">
						<el-input v-model="addForm.arrive" placeholder="请输入进站时间" style="width: 50%"></el-input>
					</el-form-item>
					<el-form-item prop="days" label="发车天数">
						<el-input-number v-model="addForm.days" controls-position="right" :precision="0" :min="1" style="width: 50%"></el-input-number>
					</el-form-item>
					<el-form-item prop="stop" label="停靠分钟">
						<el-input-number v-model="addForm.stop" controls-position="right" :precision="0" :min="0" style="width: 50%"></el-input-number>
					</el-form-item>
					<el-form-item prop="distance" label="总里程数">
						<el-input-number v-model="addForm.distance" controls-position="right" :precision="0" :min="0" style="width: 50%"></el-input-number>
					</el-form-item>
				</el-form>
				<el-button type="primary" @click="add">添加</el-button>
			</el-dialog>

			<el-dialog title="新增站台" :visible.sync="isModifying" width="30%">
				<el-form :model="modifyForm" ref="modifyForm" :rules="modifyRules">
					<el-form-item prop="station" label="到达车站">
						<el-autocomplete placeholder="请输入车站名称" v-model="modifyForm.station" :fetch-suggestions="querySearch" :trigger-on-focus="false" style="width: 50%"></el-autocomplete>
					</el-form-item>
					<el-form-item prop="arrive" label="进站时间">
						<el-input v-model="modifyForm.arrive" placeholder="请输入进站时间" style="width: 50%"></el-input>
					</el-form-item>
					<el-form-item prop="days" label="发车天数">
						<el-input-number v-model="modifyForm.days" controls-position="right" :precision="0" :min="1" style="width: 50%"></el-input-number>
					</el-form-item>
					<el-form-item prop="stop" label="停靠分钟">
						<el-input-number v-model="modifyForm.stop" controls-position="right" :precision="0" :min="0" style="width: 50%"></el-input-number>
					</el-form-item>
					<el-form-item prop="distance" label="总里程数">
						<el-input-number v-model="modifyForm.distance" controls-position="right" :precision="0" :min="0" style="width: 50%"></el-input-number>
					</el-form-item>
				</el-form>
				<el-button type="primary" @click="modifyMe">修改</el-button>
			</el-dialog>
		</div>
	</div>
</template>

<script>
import $ajax from "../../ajax";
export default {
	data() {
		return {
			train: "",
			loading: false,
			trainRoutes: [],
			stations: [],
			editing: false,
			editingStyle: "red",
			isAdding: false,
			addForm: {
				station: "",
				arrive: "",
				days: 1,
				stop: 0,
				distance: 0,
			},
			addRules: {
				station: [
					{ required: true, message: "请选择站点", trigger: "blur" },
				],
				arrive: [
					{
						required: true,
						pattern: /[0-2][0-9]:[0-6][0-9]/,
						message: "请按照HH:mm的格式进行输入",
						trigger: ["blur", "change"],
					},
				],
				days: [
					{ required: true, message: "请输入天数", trigger: "blur" },
				],
				stop: [
					{
						required: true,
						message: "请输入停靠时间",
						trigger: "blur",
					},
				],
				distance: [
					{
						required: true,
						message: "请输入里程数",
						trigger: "blur",
					},
				],
			},
			isModifying: false,
			modifyForm: {
				station: "",
				arrive: "",
				days: 1,
				stop: 0,
				distance: 0,
			},
			modifyRules: {
				station: [
					{ required: true, message: "请选择站点", trigger: "blur" },
				],
				arrive: [
					{
						required: true,
						pattern: /[0-2][0-9]:[0-6][0-9]/,
						message: "请按照HH:mm的格式进行输入",
						trigger: ["blur", "change"],
					},
				],
				days: [
					{ required: true, message: "请输入天数", trigger: "blur" },
				],
				stop: [
					{
						required: true,
						message: "请输入停靠时间",
						trigger: "blur",
					},
				],
				distance: [
					{
						required: true,
						message: "请输入里程数",
						trigger: "blur",
					},
				],
			},
		};
	},
	methods: {
		async submit() {
			let res = await $ajax.post(
				"/admin/setRoutes",
				{
					train: this.train,
					routes: JSON.stringify(this.trainRoutes),
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.$message({
					message: "列车路径设定完毕！",
					type: "success",
					duration: 1000,
				});
				this.editing = false;
			} else {
				this.$message({
					message: "列车路径设定失败！" + res.data.data,
					type: "error",
					duration: 1000,
				});
			}
		},
		add() {
			this.$refs.addForm.validate((valid) => {
				if (valid) {
					let arrive = this.addForm.arrive;
					let stop = this.addForm.stop;
					let hour = Number(arrive.split(":")[0]);
					let minute = Number(arrive.split(":")[1]) + Number(stop);
					if (minute >= 60) {
						minute -= 60;
						hour += 1;
						if (hour >= 24) {
							hour -= 24;
						}
					}
					if (minute < 10) {
						minute = "0" + minute;
					}
					if (hour < 10) {
						hour = "0" + hour;
					}
					let leave = hour + ":" + minute;
					this.addForm.leave = leave;
					this.addForm.index = this.trainRoutes.length + 1;
					this.trainRoutes.push(
						JSON.parse(JSON.stringify(this.addForm))
					);
					this.isAdding = false;
				}
			});
		},
		upMv(index) {
			if (index - 1 >= 0) {
				let a = JSON.parse(JSON.stringify(this.trainRoutes[index]));
				a.index = index;
				let b = JSON.parse(JSON.stringify(this.trainRoutes[index - 1]));
				b.index = index + 1;
				this.$set(this.trainRoutes, index - 1, a);
				this.$set(this.trainRoutes, index, b);
			}
		},
		dnMv(index) {
			if (index + 1 < this.trainRoutes.length) {
				let a = JSON.parse(JSON.stringify(this.trainRoutes[index]));
				a.index = index + 2;
				let b = JSON.parse(JSON.stringify(this.trainRoutes[index + 1]));
				b.index = index + 1;
				this.$set(this.trainRoutes, index + 1, a);
				this.$set(this.trainRoutes, index, b);
			}
		},
		modify(index) {
			this.modifyForm = JSON.parse(
				JSON.stringify(this.trainRoutes[index])
			);
			this.isModifying = true;
		},
		modifyMe() {
			this.$refs.modifyForm.validate((valid) => {
				if (valid) {
					let row = JSON.parse(JSON.stringify(this.modifyForm));
					let index = row.index;
					let arrive = row.arrive;
					let stop = row.stop;
					let hour = Number(arrive.split(":")[0]);
					let minute = Number(arrive.split(":")[1]) + Number(stop);
					if (minute >= 60) {
						minute -= 60;
						hour += 1;
						if (hour >= 24) {
							hour -= 24;
						}
					}
					if (minute < 10) {
						minute = "0" + minute;
					}
					if (hour < 10) {
						hour = "0" + hour;
					}
					let leave = hour + ":" + minute;
					row.leave = leave;
					this.$set(this.trainRoutes, index - 1, row);
					this.isModifying = false;
				}
			});
		},
		rm(index) {
			this.trainRoutes.splice(index, 1);
			for (let i = index; i < this.trainRoutes.length; i++) {
				this.trainRoutes[i].index = i + 1;
			}
		},
		async getStations() {
			let res = await $ajax.post(
				"/admin/getStations",
				{},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				let PY = require("js-pinyin");
				let QuickSort = (arr) => {
					if (arr.length <= 1) {
						return arr;
					}
					var pivotIndex = Math.floor(arr.length / 2);
					var pivot = arr.splice(pivotIndex, 1)[0];
					var left = [];
					var right = [];
					for (var i = 0; i < arr.length; i++) {
						if (PY.getFullChars(arr[i]) < PY.getFullChars(pivot)) {
							left.push(arr[i]);
						} else {
							right.push(arr[i]);
						}
					}
					return QuickSort(left).concat([pivot], QuickSort(right));
				};
				let data = QuickSort(res.data.data);
				for (let key in data) {
					this.stations.push({ value: data[key] });
				}
			}
		},
		async getTrainRoutes() {
			this.loading = true;
			let res = await $ajax.post(
				"/admin/getRoutes",
				{
					train: this.train,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.formatRoutes(res.data.data);
			}
			this.loading = false;
		},
		formatRoutes(data) {
			for (let item in data) {
				let arriveTime = data[item].arrive;
				let stop = data[item].stop;
				let hour = Number(arriveTime.split(":")[0]);
				let minute = Number(arriveTime.split(":")[1]);
				minute += stop;
				if (minute >= 60) {
					minute -= 60;
					hour += 1;
					if (hour >= 24) {
						hour -= 24;
					}
				}

				if (minute < 10) {
					minute = "0" + minute;
				}
				if (hour < 10) {
					hour = "0" + hour;
				}
				let leave = hour + ":" + minute;
				data[item].leave = leave;
			}
			this.trainRoutes = data;
		},
		querySearch(queryString, cb) {
			let results = this.stations;
			results = queryString
				? results.filter(this.createFilter(queryString))
				: results;
			//cb是回调函数，返回筛选出的结果数据到输入框下面的输入列表
			cb(results);
		},
		createFilter(queryString) {
			let PY = require("js-pinyin");
			return (item) => {
				return (
					item.value.indexOf(queryString) === 0 ||
					PY.getFullChars(item.value)
						.toLowerCase()
						.indexOf(queryString.toLowerCase()) === 0 ||
					PY.getCamelChars(item.value).indexOf(
						queryString.toUpperCase()
					) === 0
				);
			};
		},
	},
	created() {
		this.train = this.$route.params.trainId;
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
		this.getTrainRoutes();
		this.getStations();
	},
	watch: {
		async $route(to) {
			this.train = to.params.trainId;
			this.getTrainRoutes();
		},
	},
};
</script>

<style scoped>
::v-deep .el-collapse-item__content {
	padding: 0%;
}
</style>