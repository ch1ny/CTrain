<template>
	<div>
		<el-container>
			<el-main>
				<el-card>
					<el-card>
						<div style="text-align: left;" slot="header">
							<span><b>{{fromTrain}}次列车{{from}}站到{{off}}站余票查询</b></span>
						</div>
						<el-table v-loading="loading" :data="fromTickets" stripe border :cell-style="()=>{return 'text-align: center';}" :header-cell-style="()=>{return 'text-align: center';}">
							<el-table-column label="座位类型">
								<template slot-scope="scope">
									<el-radio v-model="fromSeat" :label="scope.row.type" @change="fromPrice=Number(scope.row.price)"></el-radio>
								</template>
							</el-table-column>
							<el-table-column label="剩余票数">
								<template slot-scope="scope">
									<span>余 <b :style="{'color': scope.row.num>=100?'mediumseagreen':scope.row.num >= 30?'orange':'red'}">{{scope.row.num}}</b> 张</span>
								</template>
							</el-table-column>
							<el-table-column label="预计票价">
								<template slot-scope="scope">
									<span>{{scope.row.price}}元</span>
								</template>
							</el-table-column>
						</el-table>
					</el-card>
					<el-divider><i class="iconfont icon-huochepiao" style="font-weight: bold; font-size: 20px"></i></el-divider>
					<el-card>
						<div style="text-align: left;" slot="header">
							<span><b>{{toTrain}}次列车{{on}}站到{{to}}站余票查询</b></span>
						</div>
						<el-table v-loading="loading" :data="toTickets" stripe border :cell-style="()=>{return 'text-align: center';}" :header-cell-style="()=>{return 'text-align: center';}">
							<el-table-column label="座位类型">
								<template slot-scope="scope">
									<el-radio v-model="toSeat" :label="scope.row.type" @change="toPrice=Number(scope.row.price)"></el-radio>
								</template>
							</el-table-column>
							<el-table-column label="剩余票数">
								<template slot-scope="scope">
									<span>余 <b :style="{'color': scope.row.num>=100?'mediumseagreen':scope.row.num >= 30?'orange':'red'}">{{scope.row.num}}</b> 张</span>
								</template>
							</el-table-column>
							<el-table-column label="预计票价">
								<template slot-scope="scope">
									<span>{{scope.row.price}}元</span>
								</template>
							</el-table-column>
						</el-table>
					</el-card>
					<div style="margin-top: 20px">
						<el-button @click="purchasing=true;" type="primary" style="width: 10%">购票</el-button>
					</div>
				</el-card>
			</el-main>
		</el-container>

		<div>
			<el-dialog title="乘客信息" :visible.sync="purchasing" :append-to-body="true" @open="passengers=[]">
				<el-table :data="passengers">
					<el-table-column prop="name" label="姓名"></el-table-column>
					<el-table-column prop="id" label="身份证号"></el-table-column>
					<el-table-column label="操作">
						<template slot-scope="scope">
							<el-button type="danger" @click="passengers.splice(scope.$index, 1)">移除</el-button>
						</template>
					</el-table-column>
				</el-table>
				<div style="margin-top: 10px">
					<el-button plain @click="showFriends=true;" style="width: 20%">添加乘车人</el-button>
					<el-button type="primary" @click="purchase" style="width: 20%" :disabled="passengers.length==0">确定购票</el-button>
				</div>
			</el-dialog>

			<el-dialog title="添加乘车人" :visible.sync="showFriends" :append-to-body="true">
				<el-table :data="getAccessFriends">
					<el-table-column prop="name" label="姓名"></el-table-column>
					<el-table-column prop="id" label="身份证号"></el-table-column>
					<el-table-column label="操作">
						<template slot-scope="scope">
							<el-button type="primary" @click="passengers.push(getAccessFriends[scope.$index]); showFriends=false;">选择</el-button>
						</template>
					</el-table-column>
					<div slot="append">
						<el-button @click="isAdding=true;" plain style="width: 100%; margin-bottom: 1px">新增乘车人</el-button>
					</div>
				</el-table>
			</el-dialog>

			<el-dialog title="新增乘车人" :visible.sync="isAdding" :append-to-body="true">
				<el-form :model="addForm" ref="addForm" :rules="addRules">
					<el-form-item prop="id">
						<el-input v-model="addForm.id" placeholder="请输入同行人员的身份证号码" prefix-icon="iconfont icon-credentials_icon"></el-input>
					</el-form-item>
					<el-form-item prop="name">
						<el-input v-model="addForm.name" placeholder="请输入同行人员的姓名" prefix-icon="el-icon-user"></el-input>
					</el-form-item>
				</el-form>
				<el-button type="primary" style="width: 15%" @click="add">添&nbsp;&nbsp;&nbsp;加</el-button>
			</el-dialog>
		</div>
	</div>
</template>

<script>
import axios from "axios";
import $ajax from "../ajax";
export default {
	data() {
		return {
			fromTrain: "",
			toTrain: "",
			from: "",
			off: "",
			on: "",
			to: "",
			date: "",
			fromTickets: [],
			toTickets: [],
			fromSeat: "",
			toSeat: "",
			friends: [],
			purchasing: false,
			passengers: [],
			showFriends: false,
			isAdding: false,
			addFriend: false,
			addForm: {
				id: "",
				name: "",
			},
			addRules: {
				id: [
					{
						required: true,
						message: "请输入同行人员的身份证号码",
						trigger: ["blur", "change"],
					},
				],
				name: [
					{
						required: true,
						message: "请输入同行人员的姓名",
						trigger: ["blur", "change"],
					},
				],
			},
			fromPrice: 0,
			toPrice: 0,
			loading: true,
		};
	},
	methods: {
		async getTickets(train, from, to, date) {
			let res = await $ajax.post(
				"/service/getTickets",
				{
					train: train,
					from: from,
					to: to,
					date: date,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			return res;
		},
		getNextDate(date, day) {
			var dd = new Date(date);
			dd.setDate(dd.getDate() + day);
			var y = dd.getFullYear();
			var m =
				dd.getMonth() + 1 < 10
					? "0" + (dd.getMonth() + 1)
					: dd.getMonth() + 1;
			var d = dd.getDate() < 10 ? "0" + dd.getDate() : dd.getDate();
			return y + "-" + m + "-" + d;
		},
		async getFriends() {
			let res = await $ajax.post(
				"/friend/get",
				{},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			this.friends = res.data.data;
		},
		add() {
			this.$refs.addForm.validate(async (valid) => {
				if (valid) {
					let res = await $ajax.post(
						"/friend/add",
						{
							id: this.addForm.id,
							name: this.addForm.name,
						},
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					if (res.data.code == 0) {
						let json = JSON.parse(JSON.stringify(this.addForm));
						this.friends.push(json);
						this.$message({
							message: "添加成功！",
							type: "success",
							duration: 1000,
						});
						this.isAdding = false;
					} else {
						this.$message({
							message: `添加失败！${res.data.data}`,
							type: "error",
							duration: 1000,
						});
					}
				}
			});
		},
		async purchase() {
			let indents = [];
			indents.push({
				train: this.fromTrain,
				from: this.from,
				to: this.off,
				date: this.date,
				seat: this.fromSeat,
				price: this.fromPrice,
				passengers: JSON.stringify(this.passengers),
			});
			indents.push({
				train: this.toTrain,
				from: this.on,
				to: this.to,
				date: this.getNextDate(
					this.date,
					Number(this.$route.query.days)
				),
				seat: this.toSeat,
				price: this.toPrice,
				passengers: JSON.stringify(this.passengers),
			});
			let res = await $ajax.post(
				"/service/purchase",
				{
					indents: JSON.stringify(indents),
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.$notify({
					message: "购票成功！",
					type: "success",
					duration: 1000,
					onClose: () => {
						this.$router.push({ path: "/indent" });
					},
				});
			} else {
				this.$notify({
					message: `购票失败！${res.data.data}`,
					type: "error",
					duration: 1000,
				});
			}
		},
	},
	async created() {
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
		this.fromTrain = this.$route.query.fromTrain;
		this.toTrain = this.$route.query.toTrain;
		this.from = this.$route.query.from;
		this.off = this.$route.query.off;
		this.on = this.$route.query.on;
		this.to = this.$route.query.to;
		this.date = this.$route.query.date;
		this.loading = true;
		axios
			.all([
				this.getTickets(this.fromTrain, this.from, this.off, this.date),
				this.getTickets(
					this.toTrain,
					this.on,
					this.to,
					this.getNextDate(this.date, Number(this.$route.query.days))
				),
			])
			.then(
				axios.spread((...ress) => {
					this.fromTickets = ress[0].data.data;
					this.fromSeat = this.fromTickets[0].type;
					this.fromPrice = Number(this.fromTickets[0].price);
					this.toTickets = ress[1].data.data;
					this.toSeat = this.toTickets[0].type;
					this.toPrice = Number(this.toTickets[0].price);
					this.loading = false;
				})
			);
		this.getFriends();
	},
	computed: {
		getAccessFriends() {
			return this.friends.filter((i) => {
				return this.passengers.indexOf(i) == -1;
			});
		},
	},
};
</script>